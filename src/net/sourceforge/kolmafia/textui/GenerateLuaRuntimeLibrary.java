package net.sourceforge.kolmafia.textui;

import java.util.Map;

import net.sourceforge.kolmafia.textui.parsetree.AggregateType;
import net.sourceforge.kolmafia.textui.parsetree.Function;
import net.sourceforge.kolmafia.textui.parsetree.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.kolmafia.textui.RuntimeLibrary;
import net.sourceforge.kolmafia.textui.parsetree.LibraryFunction;
import net.sourceforge.kolmafia.textui.parsetree.FunctionList;

public class GenerateLuaRuntimeLibrary {
	// No longer necessary as handled by Lua
	public static String[] skip = { "to_json", "map_to_file", "file_to_map", "count", "clear" };
	
	public static Collection<String> getMethods()
	{
		FunctionList functions = RuntimeLibrary.getFunctions();

		Map<String, String> methods = new HashMap<String, String>();

		for ( Function f : functions )
		{
			LibraryFunction func = (LibraryFunction) f;
			Type type = func.getType();
			String javaType = GenerateLuaRuntimeLibrary.getJavaType(type);
			
			String name = func.getName();
			
			Set<String> exceptions = new TreeSet<String>();
			
			if ( Arrays.asList(GenerateLuaRuntimeLibrary.skip).contains(name) )
			{
				continue;
			}
			
			if ( name == "entity_encode" || name == "entity_decode" )
			{
				exceptions.add("UnsupportedEncodingException");
			}
			
			Type[] params = func.getParams();
			
			ArrayList<String> paramList = new ArrayList<String>();
			ArrayList<String> argList = new ArrayList<String>();
			
			argList.add("");

			for ( int i = 0; i < params.length; i++ )
			{
				String paramJavaType = GenerateLuaRuntimeLibrary.getJavaType(params[i]);
				String var = String.valueOf((char)(i + 97));
				String value = GenerateLuaRuntimeLibrary.getAshValue(var, params[i]);

				if ( GenerateLuaRuntimeLibrary.isProxy( params[i] ) )
				{
					exceptions.add( "JSONException" );
				}
				
				paramList.add("final " + paramJavaType + " " + var);
				argList.add(value);
			}

			if ( javaType == "JSONObject" )
			{
				exceptions.add( "JSONException" );
			}
			
			String declaration = "public static " + javaType + " " + name + "( " + String.join(", ", paramList)  + " )";

			if ( methods.containsKey( declaration ) )
			{
				continue;
			}
			
			String superCall = "RuntimeLibrary." + name + "( interpreter" + String.join(", ", argList) + " );\n";

			String method = "	" + declaration + ( exceptions.size() > 0 ? " throws " + String.join(", ", exceptions) : "" ) + "\n"
						  + "	{\n"
						  + "		Interpreter interpreter = new Interpreter();\n";

			if ( type.getType() != DataTypes.TYPE_VOID )
			{
				method += "		Value result = " + superCall
						+ "		return " + GenerateLuaRuntimeLibrary.getTypeFunction(type) + ";\n";
			}
			else
			{
				method += "		" + superCall; 
			}

			method += "	}";
			methods.put(declaration, method);
		}

		return methods.values();
	}

	public static String getWrapped ( String type )
	{
		switch ( type )
		{
			case "int": return "Integer";
			case "char": return "Character";
			default: return type.substring(0, 1).toUpperCase() + type.substring(1);
		}
	}
	
	public static String getJavaType( Type type )
	{
		if ( GenerateLuaRuntimeLibrary.isProxy(type) )
		{
			return "JSONObject";
		}

		switch (type.getType())
		{
			case DataTypes.TYPE_INT:
			case DataTypes.TYPE_STAT:
			case DataTypes.TYPE_SLOT:
				return "int";
			case DataTypes.TYPE_FLOAT: return "double";
			case DataTypes.TYPE_STRICT_STRING:
			case DataTypes.TYPE_STRING:
				return "String";
			case DataTypes.TYPE_BUFFER: return "StringBuffer";
			case DataTypes.TYPE_MATCHER: return "Matcher";
			case DataTypes.TYPE_ANY: return "Object";
			case DataTypes.TYPE_AGGREGATE:
				if ( type instanceof AggregateType ) 
				{
					AggregateType agg = (AggregateType) type;
					
					String d = GenerateLuaRuntimeLibrary.getJavaType( agg.getDataType() );
					String i = GenerateLuaRuntimeLibrary.getJavaType( agg.getIndexType() );
					return "Map<" + GenerateLuaRuntimeLibrary.getWrapped(i) + ", " + GenerateLuaRuntimeLibrary.getWrapped(d) + ">";
				}
				return type.getName();
			case DataTypes.TYPE_RECORD:
				// Need to turn this into a defined class maybe?
				return "Object";
			default: return type.getName();
		}
	}
	
	public static String getAshType ( Type type )
	{
		if ( !( type instanceof AggregateType ) )
		{
			return "new Type( \""+ type.getName() + "\", " + type.getType() + " )";
		}

		AggregateType agg = (AggregateType) type;
		
		String dataType = GenerateLuaRuntimeLibrary.getAshType(agg.getDataType());
		
		String aggType = "new AggregateType( " + dataType + ", ";
		
		if ( agg.getSize() > -1 )
		{
			return aggType += agg.getSize() + " )";
		}

		String indexType = GenerateLuaRuntimeLibrary.getAshType(agg.getIndexType());
		
		aggType += indexType;
		
		if ( agg.getCaseInsensitive() )
		{
			aggType += ", " + agg.getSize();
		}
		
		return aggType += " )";
	}
	
	public static String getAshValue ( String var, Type type )
	{
		String ashType = GenerateLuaRuntimeLibrary.getAshType(type);
		if ( GenerateLuaRuntimeLibrary.isProxy(type) )
		{
			return "new Value( " + ashType + ", (int) " + var + ".get(\"id\"), (String) " + var + ".get(\"name\") )";
		}
		
		if ( type instanceof AggregateType )
		{
			return "new AggregateValue( " + ashType + ")";
		}
		
		switch ( type.getType() )
		{
			case DataTypes.TYPE_MATCHER:
				return "new Value( " + ashType + ", " + var + ".pattern().pattern(), " + var + " )";
			case DataTypes.TYPE_BUFFER:
				return "new Value( " + ashType + ", \"\", " + var + " )";
			default: return "new Value( " + var + " )";
		}
	}
	
	public static boolean isProxy( Type type )
	{
		return type.asProxy() != type;
	}
	
	public static String getTypeFunction( Type type )
	{
		if ( GenerateLuaRuntimeLibrary.isProxy(type) )
		{
			return "((JSONObject) result.asProxy().toJSON()).put(\"id\", (int) result.intValue())";
		}

		switch ( type.getType() )
		{
			case DataTypes.TYPE_INT:
			case DataTypes.TYPE_STAT:
			case DataTypes.TYPE_SLOT:
				return "(int) result.intValue()";
			case DataTypes.TYPE_FLOAT:
				return "result.intValue()";
			case DataTypes.TYPE_BOOLEAN:
				return "result.intValue() != 0";
			case DataTypes.TYPE_STRING:
			case DataTypes.TYPE_STRICT_STRING:
				return "result.toString()";
			case DataTypes.TYPE_BUFFER:
			case DataTypes.TYPE_MATCHER:
			case DataTypes.TYPE_AGGREGATE:
				return "(" + GenerateLuaRuntimeLibrary.getJavaType(type) + ") result.rawValue()";
			default: return "result.rawValue()";
		}
	}

	public static void main( String[] args )
	{
		String methodBody = String.join("\n\n", GenerateLuaRuntimeLibrary.getMethods());
		String classBody = "package net.sourceforge.kolmafia.textui;\n\n"
						 + "import org.json.JSONObject;\n"
						 + "import org.json.JSONException;\n\n"
						 + "import java.io.UnsupportedEncodingException;\n"
						 + "import java.util.Map;\n"
						 + "import java.util.regex.Matcher;\n\n"
					 	 + "import net.sourceforge.kolmafia.textui.Interpreter;\n"
					 	 + "import net.sourceforge.kolmafia.textui.RuntimeLibrary;\n\n"
					 	 + "import net.sourceforge.kolmafia.textui.parsetree.AggregateType;\n"
					 	 + "import net.sourceforge.kolmafia.textui.parsetree.AggregateValue;\n"
					 	 + "import net.sourceforge.kolmafia.textui.parsetree.Type;\n"
					 	 + "import net.sourceforge.kolmafia.textui.parsetree.Value;\n\n"
					 	 + "public class LuaRuntimeLibrary {\n\n"
					 	 + methodBody + "\n\n"
					 	 + "}\n";

		try
		{
			File file = new File("src/net/sourceforge/kolmafia/textui/LuaRuntimeLibrary.java");
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(classBody);
			bw.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}
}
