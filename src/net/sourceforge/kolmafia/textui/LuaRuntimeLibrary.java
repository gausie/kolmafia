package net.sourceforge.kolmafia.textui;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;

import net.sourceforge.kolmafia.textui.Interpreter;
import net.sourceforge.kolmafia.textui.RuntimeLibrary;

import net.sourceforge.kolmafia.textui.parsetree.AggregateType;
import net.sourceforge.kolmafia.textui.parsetree.AggregateValue;
import net.sourceforge.kolmafia.textui.parsetree.Type;
import net.sourceforge.kolmafia.textui.parsetree.Value;

public class LuaRuntimeLibrary {

	public static StringBuffer run_combat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.run_combat( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static boolean can_still_steal(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_still_steal( interpreter );
		return result.intValue() != 0;
	}

	public static boolean take_storage( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_storage( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Double> appearance_rates( final JSONObject a, final boolean b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.appearance_rates( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (Map<JSONObject, Double>) result.rawValue();
	}

	public static int my_lightning(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_lightning( interpreter );
		return (int) result.intValue();
	}

	public static boolean enthrone_familiar( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.enthrone_familiar( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static String leetify( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.leetify( interpreter, new Value( a ) );
		return result.toString();
	}

	public static String group( final Matcher a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.group( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return result.toString();
	}

	public static void abort(  )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.abort( interpreter );
	}

	public static void print( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.print( interpreter, new Value( a ), new Value( b ) );
	}

	public static boolean refresh_status(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.refresh_status( interpreter );
		return result.intValue() != 0;
	}

	public static JSONObject to_class( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_class( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Boolean> all_monsters_with_id(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.all_monsters_with_id( interpreter );
		return (Map<JSONObject, Boolean>) result.rawValue();
	}

	public static int damage_reduction(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.damage_reduction( interpreter );
		return (int) result.intValue();
	}

	public static String to_upper_case( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_upper_case( interpreter, new Value( a ) );
		return result.toString();
	}

	public static int buy_using_storage( final int a, final JSONObject b, final int c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy_using_storage( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ), new Value( c ) );
		return (int) result.intValue();
	}

	public static int equipped_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.equipped_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<JSONObject, Integer> get_storage(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_storage( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static void set_length( final StringBuffer a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.set_length( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ) );
	}

	public static boolean buys_item( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buys_item( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int creatable_turns( final JSONObject a, final int b, final boolean c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.creatable_turns( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( c ) );
		return (int) result.intValue();
	}

	public static boolean maximize( final String a, final int b, final int c, final boolean d )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.maximize( interpreter, new Value( a ), new Value( b ), new Value( c ), new Value( d ) );
		return result.intValue() != 0;
	}

	public static String replace_all( final Matcher a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.replace_all( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return result.toString();
	}

	public static JSONObject effect_modifier( final String a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.effect_modifier( interpreter, new Value( a ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean have_shop(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_shop( interpreter );
		return result.intValue() != 0;
	}

	public static boolean take_stash( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_stash( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static String string_modifier( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.string_modifier( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean refresh_shop(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.refresh_shop( interpreter );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Integer> get_campground(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_campground( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static void disable( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.disable( interpreter, new Value( a ) );
	}

	public static boolean take_shop( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_shop( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static StringBuffer replace( final StringBuffer a, final int b, final int c, final String d )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.replace( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ), new Value( c ), new Value( d ) );
		return (StringBuffer) result.rawValue();
	}

	public static String entity_encode( final String a ) throws UnsupportedEncodingException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.entity_encode( interpreter, new Value( a ) );
		return result.toString();
	}

	public static int gameday_to_int(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.gameday_to_int( interpreter );
		return (int) result.intValue();
	}

	public static boolean put_shop( final int a, final int b, final JSONObject c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_shop( interpreter, new Value( a ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean property_has_default( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.property_has_default( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static Object svn_info( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.svn_info( interpreter, new Value( a ) );
		return result.rawValue();
	}

	public static boolean knoll_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.knoll_available( interpreter );
		return result.intValue() != 0;
	}

	public static boolean in_mysticality_sign(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_mysticality_sign( interpreter );
		return result.intValue() != 0;
	}

	public static String get_player_id( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_player_id( interpreter, new Value( a ) );
		return result.toString();
	}

	public static boolean reprice_shop( final int a, final int b, final JSONObject c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reprice_shop( interpreter, new Value( a ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ) );
		return result.intValue() != 0;
	}

	public static void enable( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.enable( interpreter, new Value( a ) );
	}

	public static JSONObject last_monster(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_monster( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer replace_string( final String a, final String b, final String c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.replace_string( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean gnomads_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.gnomads_available( interpreter );
		return result.intValue() != 0;
	}

	public static Map<Integer, String> session_logs( final String a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.session_logs( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static int mana_cost_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mana_cost_modifier( interpreter );
		return (int) result.intValue();
	}

	public static int weapon_type( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.weapon_type( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int to_int( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_int( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject to_servant( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_servant( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int lightning_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.lightning_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static String every_card_name( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.every_card_name( interpreter, new Value( a ) );
		return result.toString();
	}

	public static void writeln( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.writeln( interpreter, new Value( a ) );
	}

	public static boolean in_moxie_sign(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_moxie_sign( interpreter );
		return result.intValue() != 0;
	}

	public static int my_rain(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_rain( interpreter );
		return (int) result.intValue();
	}

	public static boolean adventure( final int a, final JSONObject b, final String c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.adventure( interpreter, new Value( a ), new Value( new Type( "location", 101 ), (int) b.get("id"), (String) b.get("name") ), new Value( c ) );
		return result.intValue() != 0;
	}

	public static JSONObject minstrel_instrument(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.minstrel_instrument( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Integer> get_free_pulls(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_free_pulls( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static int last_decision(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_decision( interpreter );
		return (int) result.intValue();
	}

	public static int rain_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.rain_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean outfit( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.outfit( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int get_clan_id(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_clan_id( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject image_to_monster( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.image_to_monster( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static JSONObject my_poke_fam( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_poke_fam( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static String my_hash(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_hash( interpreter );
		return result.toString();
	}

	public static double to_float( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_float( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static JSONObject to_item( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_item( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int get_fuel(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_fuel( interpreter );
		return (int) result.intValue();
	}

	public static int group_count( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.group_count( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return (int) result.intValue();
	}

	public static String craft_type( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.craft_type( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static String holiday(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.holiday( interpreter );
		return result.toString();
	}

	public static Map<Integer, String> get_moods(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_moods( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static boolean hermit( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.hermit( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_location( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_location( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Boolean> favorite_familiars(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.favorite_familiars( interpreter );
		return (Map<JSONObject, Boolean>) result.rawValue();
	}

	public static boolean use_servant( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use_servant( interpreter, new Value( new Type( "servant", 114 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static void batch_open(  )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.batch_open( interpreter );
	}

	public static String char_at( final String a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.char_at( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static JSONObject my_effective_familiar(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_effective_familiar( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<String, String> form_fields(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.form_fields( interpreter );
		return (Map<String, String>) result.rawValue();
	}

	public static Map<Integer, String> get_shop_log(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_shop_log( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static Map<Integer, JSONObject> sweet_synthesis_pairing( final JSONObject a, final JSONObject b, final int c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis_pairing( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ), new Value( c ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static boolean have_equipped( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_equipped( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static double square_root( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.square_root( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static void traceprint( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.traceprint( interpreter, new Value( a ) );
	}

	public static int to_stat( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_stat( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static String last_skill_message(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_skill_message( interpreter );
		return result.toString();
	}

	public static int my_maxmp(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_maxmp( interpreter );
		return (int) result.intValue();
	}

	public static boolean in_bad_moon(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_bad_moon( interpreter );
		return result.intValue() != 0;
	}

	public static boolean boolean_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.boolean_modifier( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Integer> get_stash(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_stash( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static boolean tower_door(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.tower_door( interpreter );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Integer> item_drops(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_drops( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static int familiar_weight( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.familiar_weight( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static double expression_eval( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.expression_eval( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static int index_of( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.index_of( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static JSONObject to_bounty( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_bounty( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean sell( final JSONObject a, final int b, final JSONObject c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sell( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_phylum( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_phylum( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean put_closet( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_closet( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, JSONObject> candy_for_tier( final int a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.candy_for_tier( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static int closet_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.closet_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int autosell_price( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.autosell_price( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject equipped_item( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.equipped_item( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer steal(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.steal( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static int my_inebriety(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_inebriety( interpreter );
		return (int) result.intValue();
	}

	public static boolean maximize( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.maximize( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static Matcher reset( final Matcher a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reset( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return (Matcher) result.rawValue();
	}

	public static void write( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.write( interpreter, new Value( a ) );
	}

	public static int current_mcd(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.current_mcd( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject to_skill( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_skill( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static double item_drop_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_drop_modifier( interpreter );
		return result.intValue();
	}

	public static boolean have_chef(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_chef( interpreter );
		return result.intValue() != 0;
	}

	public static JSONObject my_location(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_location( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static void waitq( final int a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.waitq( interpreter, new Value( a ) );
	}

	public static int meat_drop(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.meat_drop( interpreter );
		return (int) result.intValue();
	}

	public static double min( final double a, final double b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.min( interpreter, new Value( a ), new Value( b ) );
		return result.intValue();
	}

	public static int round( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.round( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean bjornify_familiar( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.bjornify_familiar( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_skill( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_skill( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int creatable_turns( final JSONObject a, final int b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.creatable_turns( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (int) result.intValue();
	}

	public static boolean rename_property( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.rename_property( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static boolean mmg_retract_bet( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_retract_bet( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int to_int( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_int( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static void wait( final int a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.wait( interpreter, new Value( a ) );
	}

	public static int get_auto_attack(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_auto_attack( interpreter );
		return (int) result.intValue();
	}

	public static int monster_attack( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_attack( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static StringBuffer runaway(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.runaway( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static boolean equip( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.equip( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int my_maxfury(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_maxfury( interpreter );
		return (int) result.intValue();
	}

	public static boolean is_discardable( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_discardable( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int weight_adjustment(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.weight_adjustment( interpreter );
		return (int) result.intValue();
	}

	public static int historical_price( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.historical_price( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<Integer, Integer> mmg_offered_bets(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_offered_bets( interpreter );
		return (Map<Integer, Integer>) result.rawValue();
	}

	public static boolean restore_mp( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.restore_mp( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int my_maxhp(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_maxhp( interpreter );
		return (int) result.intValue();
	}

	public static boolean can_drink(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_drink( interpreter );
		return result.intValue() != 0;
	}

	public static JSONObject my_servant(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_servant( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer use_skill( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use_skill( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean put_shop_using_storage( final int a, final int b, final int c, final JSONObject d ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_shop_using_storage( interpreter, new Value( a ), new Value( b ), new Value( c ), new Value( new Type( "item", 100 ), (int) d.get("id"), (String) d.get("name") ) );
		return result.intValue() != 0;
	}

	public static int random( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.random( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static int my_session_meat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_session_meat( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject my_class(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_class( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static void abort( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.abort( interpreter, new Value( a ) );
	}

	public static int jump_chance( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.jump_chance( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int my_discomomentum(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_discomomentum( interpreter );
		return (int) result.intValue();
	}

	public static Map<Integer, Integer> reverse_numberology( final int a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reverse_numberology( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, Integer>) result.rawValue();
	}

	public static void mmg_visit(  )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.mmg_visit( interpreter );
	}

	public static int stills_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stills_available( interpreter );
		return (int) result.intValue();
	}

	public static int inebriety_limit(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.inebriety_limit( interpreter );
		return (int) result.intValue();
	}

	public static String my_path(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_path( interpreter );
		return result.toString();
	}

	public static int stat_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stat_modifier( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (int) result.intValue();
	}

	public static boolean can_equip( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_equip( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static String get_property( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_property( interpreter, new Value( a ) );
		return result.toString();
	}

	public static String limit_mode(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.limit_mode( interpreter );
		return result.toString();
	}

	public static int my_buffedstat( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_buffedstat( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean use_skill( final int a, final JSONObject b, final String c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use_skill( interpreter, new Value( a ), new Value( new Type( "skill", 104 ), (int) b.get("id"), (String) b.get("name") ), new Value( c ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_item( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_item( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static void council(  )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.council( interpreter );
	}

	public static String get_clan_name(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_clan_name( interpreter );
		return result.toString();
	}

	public static int tavern( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.tavern( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean minstrel_quest(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.minstrel_quest( interpreter );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Integer> get_clan_lounge(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_clan_lounge( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static int have_effect( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_effect( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject monster_phylum( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_phylum( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer run_choice( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.run_choice( interpreter, new Value( a ) );
		return (StringBuffer) result.rawValue();
	}

	public static void print( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.print( interpreter, new Value( a ) );
	}

	public static JSONObject get_dwelling(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_dwelling( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean have_bartender(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_bartender( interpreter );
		return result.intValue() != 0;
	}

	public static String get_property( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_property( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean take_closet( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_closet( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static boolean boolean_modifier( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.boolean_modifier( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static boolean is_wearing_outfit( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_wearing_outfit( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int mmg_wait_event( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_wait_event( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static JSONObject skill_modifier( final String a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.skill_modifier( interpreter, new Value( a ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int last_index_of( final String a, final String b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_index_of( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return (int) result.intValue();
	}

	public static String to_plural( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_plural( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static Map<JSONObject, Integer> get_chateau(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_chateau( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static boolean is_trendy( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_trendy( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static boolean ends_with( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.ends_with( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static int current_hit_stat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.current_hit_stat( interpreter );
		return (int) result.intValue();
	}

	public static boolean is_integer( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_integer( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static StringBuffer append_tail( final Matcher a, final StringBuffer b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.append_tail( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( new Type( "buffer", 6 ), "", b ) );
		return (StringBuffer) result.rawValue();
	}

	public static StringBuffer visit_url(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.visit_url( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static JSONObject to_coinmaster( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_coinmaster( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean contains_text( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.contains_text( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static int jump_chance( final JSONObject a, final int b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.jump_chance( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (int) result.intValue();
	}

	public static int extract_meat( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.extract_meat( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean is_coinmaster_item( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_coinmaster_item( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int buffed_hit_stat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buffed_hit_stat( interpreter );
		return (int) result.intValue();
	}

	public static int my_basestat( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_basestat( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static String time_to_string(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.time_to_string( interpreter );
		return result.toString();
	}

	public static boolean cli_execute( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.cli_execute( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static String format_date_time( final String a, final String b, final String c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.format_date_time( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return result.toString();
	}

	public static boolean take_closet( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_closet( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_monster( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_monster( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean florist_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.florist_available( interpreter );
		return result.intValue() != 0;
	}

	public static boolean have_mushroom_plot(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_mushroom_plot( interpreter );
		return result.intValue() != 0;
	}

	public static int spleen_limit(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.spleen_limit( interpreter );
		return (int) result.intValue();
	}

	public static String url_decode( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.url_decode( interpreter, new Value( a ) );
		return result.toString();
	}

	public static void mmg_search( final int a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.mmg_search( interpreter, new Value( a ), new Value( b ) );
	}

	public static int jump_chance(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.jump_chance( interpreter );
		return (int) result.intValue();
	}

	public static int monster_initiative( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_initiative( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<JSONObject, Integer> get_closet(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_closet( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static JSONObject to_element( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_element( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int floor( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.floor( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static void chat_private( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.chat_private( interpreter, new Value( a ), new Value( b ) );
	}

	public static JSONObject my_bjorned_familiar(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_bjorned_familiar( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean canadia_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.canadia_available( interpreter );
		return result.intValue() != 0;
	}

	public static double elemental_resistance(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.elemental_resistance( interpreter );
		return result.intValue();
	}

	public static int mmg_bet_amount( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_amount( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static String get_path_variables(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_path_variables( interpreter );
		return result.toString();
	}

	public static Map<JSONObject, Integer> get_shop(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_shop( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static int shop_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.shop_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static String string_modifier( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.string_modifier( interpreter, new Value( a ) );
		return result.toString();
	}

	public static String get_ccs_action( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_ccs_action( interpreter, new Value( a ) );
		return result.toString();
	}

	public static int slash_count( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.slash_count( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int to_slot( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_slot( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static int jump_chance( final JSONObject a, final int b, final int c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.jump_chance( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( c ) );
		return (int) result.intValue();
	}

	public static String my_id(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_id( interpreter );
		return result.toString();
	}

	public static Map<Integer, String> mood_list(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mood_list( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static boolean svn_at_head( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.svn_at_head( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static JSONObject dad_sea_monkee_weakness( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.dad_sea_monkee_weakness( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean drink( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.drink( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int mmg_bet_owner_id( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_owner_id( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean property_exists( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.property_exists( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static boolean buy( final JSONObject a, final int b, final JSONObject c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean choice_follows_fight(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.choice_follows_fight( interpreter );
		return result.intValue() != 0;
	}

	public static int display_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.display_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static double to_float( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_float( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static boolean batch_close(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.batch_close( interpreter );
		return result.intValue() != 0;
	}

	public static String replace_first( final Matcher a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.replace_first( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return result.toString();
	}

	public static JSONObject to_vykea( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_vykea( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean have_outfit( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_outfit( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static String last_item_message(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_item_message( interpreter );
		return result.toString();
	}

	public static boolean have_familiar( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_familiar( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int my_mp(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_mp( interpreter );
		return (int) result.intValue();
	}

	public static boolean eudora( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.eudora( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static JSONObject to_thrall( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_thrall( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static double to_float( final boolean a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_float( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static int stat_bonus_tomorrow(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stat_bonus_tomorrow( interpreter );
		return (int) result.intValue();
	}

	public static boolean is_npc_item( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_npc_item( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static void remove_item_condition( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.remove_item_condition( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
	}

	public static boolean restore_hp( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.restore_hp( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int mall_prices( final Map<Integer, Boolean> a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mall_prices( interpreter, new AggregateValue( new AggregateType( new Type( "boolean", 2 ), new Type( "int", 3 ) )) );
		return (int) result.intValue();
	}

	public static boolean sweet_synthesis( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static String group( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.group( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return result.toString();
	}

	public static boolean have_servant( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_servant( interpreter, new Value( new Type( "servant", 114 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean chew( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.chew( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static StringBuffer visit_url( final String a, final boolean b, final boolean c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.visit_url( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static Map<Integer, String> session_logs( final String a, final String b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.session_logs( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static StringBuffer append( final StringBuffer a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.append( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean refresh_stash(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.refresh_stash( interpreter );
		return result.intValue() != 0;
	}

	public static void mood_execute( final int a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.mood_execute( interpreter, new Value( a ) );
	}

	public static Map<Integer, Object> maximize( final String a, final int b, final int c, final boolean d, final boolean e )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.maximize( interpreter, new Value( a ), new Value( b ), new Value( c ), new Value( d ), new Value( e ) );
		return (Map<Integer, Object>) result.rawValue();
	}

	public static Matcher create_matcher( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.create_matcher( interpreter, new Value( a ), new Value( b ) );
		return (Matcher) result.rawValue();
	}

	public static int last_index_of( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_index_of( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static boolean in_muscle_sign(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_muscle_sign( interpreter );
		return result.intValue() != 0;
	}

	public static boolean use( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static String substring( final String a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.substring( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean autosell( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.autosell( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static double modifier_eval( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.modifier_eval( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static int my_absorbs(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_absorbs( interpreter );
		return (int) result.intValue();
	}

	public static int turns_per_cast( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.turns_per_cast( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject my_vykea_companion(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_vykea_companion( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<String, Boolean> who_clan(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.who_clan( interpreter );
		return (Map<String, Boolean>) result.rawValue();
	}

	public static int monster_defense( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_defense( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<Integer, JSONObject> candy_for_tier( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.candy_for_tier( interpreter, new Value( a ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static int meat_drop( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.meat_drop( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject to_effect( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_effect( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static double numeric_modifier( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.numeric_modifier( interpreter, new Value( a ), new Value( b ) );
		return result.intValue();
	}

	public static int my_primestat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_primestat( interpreter );
		return (int) result.intValue();
	}

	public static Matcher reset( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reset( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return (Matcher) result.rawValue();
	}

	public static boolean is_tradeable( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_tradeable( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int index_of( final String a, final String b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.index_of( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return (int) result.intValue();
	}

	public static Map<String, Boolean> get_all_properties( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_all_properties( interpreter, new Value( a ), new Value( b ) );
		return (Map<String, Boolean>) result.rawValue();
	}

	public static int mall_prices( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mall_prices( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static int pulls_remaining(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.pulls_remaining( interpreter );
		return (int) result.intValue();
	}

	public static boolean reprice_shop( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reprice_shop( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static double historical_age( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.historical_age( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue();
	}

	public static JSONObject to_servant( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_servant( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int expected_damage(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.expected_damage( interpreter );
		return (int) result.intValue();
	}

	public static boolean sells_item( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sells_item( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, Object> item_drops_array( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_drops_array( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<Integer, Object>) result.rawValue();
	}

	public static boolean is_familiar_equipment_locked(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_familiar_equipment_locked( interpreter );
		return result.intValue() != 0;
	}

	public static boolean buy_using_storage( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy_using_storage( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int ceil( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.ceil( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean is_banished( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_banished( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean property_exists( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.property_exists( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static int total_free_rests(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.total_free_rests( interpreter );
		return (int) result.intValue();
	}

	public static String to_string( final double a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_string( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean find( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.find( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return result.intValue() != 0;
	}

	public static boolean is_giftable( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_giftable( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, String> xpath( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.xpath( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static StringBuffer run_choice( final int a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.run_choice( interpreter, new Value( a ), new Value( b ) );
		return (StringBuffer) result.rawValue();
	}

	public static int pvp_attacks_left(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.pvp_attacks_left( interpreter );
		return (int) result.intValue();
	}

	public static String to_string( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_string( interpreter, new Value( a ) );
		return result.toString();
	}

	public static boolean put_stash( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_stash( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int rollover(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.rollover( interpreter );
		return (int) result.intValue();
	}

	public static int combat_mana_cost_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.combat_mana_cost_modifier( interpreter );
		return (int) result.intValue();
	}

	public static Map<Integer, JSONObject> sweet_synthesis_pairing( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis_pairing( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static boolean user_confirm( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.user_confirm( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, Object> item_drops_array(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_drops_array( interpreter );
		return (Map<Integer, Object>) result.rawValue();
	}

	public static String today_to_string(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.today_to_string( interpreter );
		return result.toString();
	}

	public static int available_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.available_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static String get_counters( final String a, final int b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_counters( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return result.toString();
	}

	public static int monster_defense(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_defense( interpreter );
		return (int) result.intValue();
	}

	public static double elemental_resistance( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.elemental_resistance( interpreter, new Value( new Type( "element", 109 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue();
	}

	public static String eudora(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.eudora( interpreter );
		return result.toString();
	}

	public static int my_ascensions(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_ascensions( interpreter );
		return (int) result.intValue();
	}

	public static Map<JSONObject, Integer> item_drops( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_drops( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static void print_html( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.print_html( interpreter, new Value( a ) );
	}

	public static int my_spleen_use(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_spleen_use( interpreter );
		return (int) result.intValue();
	}

	public static int my_meat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_meat( interpreter );
		return (int) result.intValue();
	}

	public static int get_revision(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_revision( interpreter );
		return (int) result.intValue();
	}

	public static Map<Integer, JSONObject> sweet_synthesis_pair( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis_pair( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static String my_companion(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_companion( interpreter );
		return result.toString();
	}

	public static Map<Integer, String> get_goals(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_goals( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static int raw_damage_absorption(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.raw_damage_absorption( interpreter );
		return (int) result.intValue();
	}

	public static boolean sweet_synthesis( final JSONObject a, final int b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static String my_name(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_name( interpreter );
		return result.toString();
	}

	public static JSONObject desc_to_effect( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.desc_to_effect( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int mmg_bet_winnings(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_winnings( interpreter );
		return (int) result.intValue();
	}

	public static String now_to_string( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.now_to_string( interpreter, new Value( a ) );
		return result.toString();
	}

	public static JSONObject effect_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.effect_modifier( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static String gameday_to_string(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.gameday_to_string( interpreter );
		return result.toString();
	}

	public static Map<Integer, String> available_choice_options(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.available_choice_options( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static String mmg_bet_owner( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_owner( interpreter, new Value( a ) );
		return result.toString();
	}

	public static int get_power( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_power( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int length( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.length( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static String form_field( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.form_field( interpreter, new Value( a ) );
		return result.toString();
	}

	public static Map<Integer, String> session_logs( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.session_logs( interpreter, new Value( a ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static int current_rad_sickness(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.current_rad_sickness( interpreter );
		return (int) result.intValue();
	}

	public static boolean take_display( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_display( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean retrieve_item( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.retrieve_item( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean eat( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.eat( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int my_adventures(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_adventures( interpreter );
		return (int) result.intValue();
	}

	public static int my_closet_meat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_closet_meat( interpreter );
		return (int) result.intValue();
	}

	public static boolean have_skill( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_skill( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static StringBuffer replace_string( final StringBuffer a, final String b, final String c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.replace_string( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static JSONObject to_monster( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_monster( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Double> appearance_rates( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.appearance_rates( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<JSONObject, Double>) result.rawValue();
	}

	public static int my_audience(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_audience( interpreter );
		return (int) result.intValue();
	}

	public static double to_float( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_float( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static StringBuffer delete( final StringBuffer a, final int b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.delete( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static int storage_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.storage_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int my_hp(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_hp( interpreter );
		return (int) result.intValue();
	}

	public static String to_string( final int a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_string( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean take_shop( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.take_shop( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static void chat_clan( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.chat_clan( interpreter, new Value( a ) );
	}

	public static boolean guild_store_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.guild_store_available( interpreter );
		return result.intValue() != 0;
	}

	public static boolean have_display(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.have_display( interpreter );
		return result.intValue() != 0;
	}

	public static int adv_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.adv_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static double numeric_modifier( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.numeric_modifier( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static JSONObject to_class( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_class( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer visit_url( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.visit_url( interpreter, new Value( a ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean get_ignore_zone_warnings(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_ignore_zone_warnings( interpreter );
		return result.intValue() != 0;
	}

	public static int stash_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stash_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int stat_bonus_today(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stat_bonus_today( interpreter );
		return (int) result.intValue();
	}

	public static String entity_decode( final String a ) throws UnsupportedEncodingException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.entity_decode( interpreter, new Value( a ) );
		return result.toString();
	}

	public static int total_turns_played(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.total_turns_played( interpreter );
		return (int) result.intValue();
	}

	public static String to_lower_case( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_lower_case( interpreter, new Value( a ) );
		return result.toString();
	}

	public static Map<Integer, JSONObject> get_monsters( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_monsters( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static JSONObject my_familiar(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_familiar( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static void chat_notify( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.chat_notify( interpreter, new Value( a ), new Value( b ) );
	}

	public static int my_daycount(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_daycount( interpreter );
		return (int) result.intValue();
	}

	public static boolean adventure( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.adventure( interpreter, new Value( a ), new Value( new Type( "location", 101 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static void chat_macro( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.chat_macro( interpreter, new Value( a ) );
	}

	public static boolean can_eat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_eat( interpreter );
		return result.intValue() != 0;
	}

	public static void lock_familiar_equipment( final boolean a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.lock_familiar_equipment( interpreter, new Value( a ) );
	}

	public static int minstrel_level(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.minstrel_level( interpreter );
		return (int) result.intValue();
	}

	public static String outfit_tattoo( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.outfit_tattoo( interpreter, new Value( a ) );
		return result.toString();
	}

	public static boolean adv1( final JSONObject a, final int b, final String c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.adv1( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( c ) );
		return result.intValue() != 0;
	}

	public static int my_storage_meat(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_storage_meat( interpreter );
		return (int) result.intValue();
	}

	public static int monster_hp( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_hp( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean hidden_temple_unlocked(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.hidden_temple_unlocked( interpreter );
		return result.intValue() != 0;
	}

	public static String remove_property( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.remove_property( interpreter, new Value( a ), new Value( b ) );
		return result.toString();
	}

	public static boolean sweet_synthesis( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int my_thunder(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_thunder( interpreter );
		return (int) result.intValue();
	}

	public static Map<Integer, String> split_string( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.split_string( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static void logprint( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.logprint( interpreter, new Value( a ) );
	}

	public static Map<JSONObject, Boolean> get_location_monsters( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_location_monsters( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<JSONObject, Boolean>) result.rawValue();
	}

	public static int thunder_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.thunder_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject to_familiar( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_familiar( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static double monster_eval( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_eval( interpreter, new Value( a ) );
		return result.intValue();
	}

	public static String inaccessible_reason( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.inaccessible_reason( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static int monster_factoids_available( final JSONObject a, final boolean b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_factoids_available( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (int) result.intValue();
	}

	public static JSONObject stun_skill(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.stun_skill( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int buy_price( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy_price( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean flush_monster_manuel_cache(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.flush_monster_manuel_cache( interpreter );
		return result.intValue() != 0;
	}

	public static int mmg_make_bet( final int a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_make_bet( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static void stop_counter( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.stop_counter( interpreter, new Value( a ) );
	}

	public static Map<Integer, String> available_choice_options( final boolean a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.available_choice_options( interpreter, new Value( a ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static double numeric_modifier( final JSONObject a, final String b, final int c, final JSONObject d ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.numeric_modifier( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ), new Value( c ), new Value( new Type( "item", 100 ), (int) d.get("id"), (String) d.get("name") ) );
		return result.intValue();
	}

	public static String mmg_bet_taker_id(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_taker_id( interpreter );
		return result.toString();
	}

	public static boolean is_displayable( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_displayable( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int creatable_turns( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.creatable_turns( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int fuel_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.fuel_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static JSONObject my_thrall(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_thrall( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static JSONObject to_skill( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_skill( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int creatable_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.creatable_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int my_level(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_level( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject to_item( final String a, final int b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_item( interpreter, new Value( a ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int turns_played(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.turns_played( interpreter );
		return (int) result.intValue();
	}

	public static int max( final int a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.max( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static boolean equip( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.equip( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static String remove_property( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.remove_property( interpreter, new Value( a ) );
		return result.toString();
	}

	public static boolean hippy_store_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.hippy_store_available( interpreter );
		return result.intValue() != 0;
	}

	public static String get_player_name( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_player_name( interpreter, new Value( a ) );
		return result.toString();
	}

	public static double combat_rate_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.combat_rate_modifier( interpreter );
		return result.intValue();
	}

	public static void set_auto_attack( final String a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.set_auto_attack( interpreter, new Value( a ) );
	}

	public static boolean boolean_modifier( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.boolean_modifier( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static void set_auto_attack( final int a )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.set_auto_attack( interpreter, new Value( a ) );
	}

	public static boolean starts_with( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.starts_with( interpreter, new Value( a ), new Value( b ) );
		return result.intValue() != 0;
	}

	public static double experience_bonus(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.experience_bonus( interpreter );
		return result.intValue();
	}

	public static int truncate( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.truncate( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static boolean create( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.create( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static Map<JSONObject, Map<Integer, String>> get_florist_plants(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_florist_plants( interpreter );
		return (Map<JSONObject, Map<Integer, String>>) result.rawValue();
	}

	public static boolean hippy_stone_broken(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.hippy_stone_broken( interpreter );
		return result.intValue() != 0;
	}

	public static boolean in_hardcore(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_hardcore( interpreter );
		return result.intValue() != 0;
	}

	public static int sell_price( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sell_price( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<Integer, Map<Integer, String>> group_string( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.group_string( interpreter, new Value( a ), new Value( b ) );
		return (Map<Integer, Map<Integer, String>>) result.rawValue();
	}

	public static JSONObject to_location( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_location( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static String to_url( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_url( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static boolean user_confirm( final String a, final int b, final boolean c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.user_confirm( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return result.intValue() != 0;
	}

	public static boolean dispensary_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.dispensary_available( interpreter );
		return result.intValue() != 0;
	}

	public static boolean empty_closet(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.empty_closet( interpreter );
		return result.intValue() != 0;
	}

	public static boolean use_skill( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use_skill( interpreter, new Value( a ), new Value( new Type( "skill", 104 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static int monster_hp(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_hp( interpreter );
		return (int) result.intValue();
	}

	public static int to_int( final double a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_int( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static int buy( final int a, final JSONObject b, final int c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ), new Value( c ) );
		return (int) result.intValue();
	}

	public static int mp_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mp_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean to_boolean( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_boolean( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static StringBuffer throw_items( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.throw_items( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return (StringBuffer) result.rawValue();
	}

	public static int monster_attack(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_attack( interpreter );
		return (int) result.intValue();
	}

	public static void print(  )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.print( interpreter );
	}

	public static String make_url( final String a, final boolean b, final boolean c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.make_url( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return result.toString();
	}

	public static int my_soulsauce(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_soulsauce( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject sweet_synthesis_result( final JSONObject a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis_result( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static void chat_clan( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.chat_clan( interpreter, new Value( a ), new Value( b ) );
	}

	public static boolean goal_exists( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.goal_exists( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, Integer> mmg_my_bets(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_my_bets( interpreter );
		return (Map<Integer, Integer>) result.rawValue();
	}

	public static void add_item_condition( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.add_item_condition( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
	}

	public static boolean overdrink( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.overdrink( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean use_familiar( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.use_familiar( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean svn_exists( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.svn_exists( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static boolean equip_all_familiars(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.equip_all_familiars( interpreter );
		return result.intValue() != 0;
	}

	public static double meat_drop_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.meat_drop_modifier( interpreter );
		return result.intValue();
	}

	public static String mmg_bet_taker(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_bet_taker( interpreter );
		return result.toString();
	}

	public static JSONObject monster_element(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_element( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int moon_phase(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.moon_phase( interpreter );
		return (int) result.intValue();
	}

	public static boolean can_interact(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_interact( interpreter );
		return result.intValue() != 0;
	}

	public static int end( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.end( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return (int) result.intValue();
	}

	public static int end( final Matcher a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.end( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static Map<JSONObject, Integer> get_ingredients( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_ingredients( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static StringBuffer run_combat( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.run_combat( interpreter, new Value( a ) );
		return (StringBuffer) result.rawValue();
	}

	public static JSONObject monster_element( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_element( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static boolean change_mcd( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.change_mcd( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int mall_prices( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mall_prices( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static int my_turncount(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_turncount( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject to_effect( final String a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_effect( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int mmg_take_bet( final int a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mmg_take_bet( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static void set_location( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.set_location( interpreter, new Value( new Type( "location", 101 ), (int) a.get("id"), (String) a.get("name") ) );
	}

	public static int start( final Matcher a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.start( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ) );
		return (int) result.intValue();
	}

	public static int moon_light(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.moon_light( interpreter );
		return (int) result.intValue();
	}

	public static void set_property( final String a, final String b )
	{
		Interpreter interpreter = new Interpreter();
		RuntimeLibrary.set_property( interpreter, new Value( a ), new Value( b ) );
	}

	public static String property_default_value( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.property_default_value( interpreter, new Value( a ) );
		return result.toString();
	}

	public static Map<JSONObject, Integer> get_related( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_related( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static boolean put_shop_using_storage( final int a, final int b, final JSONObject c ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_shop_using_storage( interpreter, new Value( a ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject class_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.class_modifier( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static double initiative_modifier(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.initiative_modifier( interpreter );
		return result.intValue();
	}

	public static int min( final int a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.min( interpreter, new Value( a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static JSONObject skill_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.skill_modifier( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Integer> get_inventory(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_inventory( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static String get_version(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_version( interpreter );
		return result.toString();
	}

	public static String get_path_full(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_path_full( interpreter );
		return result.toString();
	}

	public static boolean is_trendy( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_trendy( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean put_shop( final int a, final int b, final int c, final JSONObject d ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_shop( interpreter, new Value( a ), new Value( b ), new Value( c ), new Value( new Type( "item", 100 ), (int) d.get("id"), (String) d.get("name") ) );
		return result.intValue() != 0;
	}

	public static int shop_price( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.shop_price( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int npc_price( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.npc_price( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<Integer, String> split_string( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.split_string( interpreter, new Value( a ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static double damage_absorption_percent(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.damage_absorption_percent( interpreter );
		return result.intValue();
	}

	public static Map<Integer, JSONObject> outfit_pieces( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.outfit_pieces( interpreter, new Value( a ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static boolean hedge_maze( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.hedge_maze( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static boolean is_accessible( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_accessible( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean is_unrestricted( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_unrestricted( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static Map<Integer, Integer> reverse_numberology(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.reverse_numberology( interpreter );
		return (Map<Integer, Integer>) result.rawValue();
	}

	public static StringBuffer visit_url( final String a, final boolean b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.visit_url( interpreter, new Value( a ), new Value( b ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean faxbot( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.faxbot( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean will_usually_miss(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.will_usually_miss( interpreter );
		return result.intValue() != 0;
	}

	public static StringBuffer insert( final StringBuffer a, final int b, final String c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.insert( interpreter, new Value( new Type( "buffer", 6 ), "", a ), new Value( b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static Map<Integer, Object> get_stack_trace(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_stack_trace( interpreter );
		return (Map<Integer, Object>) result.rawValue();
	}

	public static boolean is_unrestricted( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_unrestricted( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject my_enthroned_familiar(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_enthroned_familiar( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static JSONObject to_effect( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_effect( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static JSONObject familiar_equipment( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.familiar_equipment( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static int my_session_adv(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_session_adv( interpreter );
		return (int) result.intValue();
	}

	public static int start( final Matcher a, final int b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.start( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( b ) );
		return (int) result.intValue();
	}

	public static int weapon_hands( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.weapon_hands( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean eatsilent( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.eatsilent( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject monster_phylum(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_phylum( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static StringBuffer attack(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.attack( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static StringBuffer append_replacement( final Matcher a, final StringBuffer b, final String c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.append_replacement( interpreter, new Value( new Type( "matcher", 7 ), a.pattern().pattern(), a ), new Value( new Type( "buffer", 6 ), "", b ), new Value( c ) );
		return (StringBuffer) result.rawValue();
	}

	public static boolean put_closet( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_closet( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean drinksilent( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.drinksilent( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean white_citadel_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.white_citadel_available( interpreter );
		return result.intValue() != 0;
	}

	public static Map<Integer, JSONObject> sweet_synthesis_pair( final JSONObject a, final int b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.sweet_synthesis_pair( interpreter, new Value( new Type( "effect", 105 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return (Map<Integer, JSONObject>) result.rawValue();
	}

	public static int monster_level_adjustment(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_level_adjustment( interpreter );
		return (int) result.intValue();
	}

	public static int gametime_to_int(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.gametime_to_int( interpreter );
		return (int) result.intValue();
	}

	public static Map<JSONObject, Integer> extract_items( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.extract_items( interpreter, new Value( a ) );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static StringBuffer throw_item( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.throw_item( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (StringBuffer) result.rawValue();
	}

	public static int soulsauce_cost( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.soulsauce_cost( interpreter, new Value( new Type( "skill", 104 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static String my_mask(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_mask( interpreter );
		return result.toString();
	}

	public static boolean to_boolean( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_boolean( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int fullness_limit(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.fullness_limit( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject familiar_equipped_equipment( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.familiar_equipped_equipment( interpreter, new Value( new Type( "familiar", 106 ), (int) a.get("id"), (String) a.get("name") ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static String my_sign(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_sign( interpreter );
		return result.toString();
	}

	public static boolean can_faxbot( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.can_faxbot( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static boolean visit( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.visit( interpreter, new Value( new Type( "coinmaster", 110 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject class_modifier( final String a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.class_modifier( interpreter, new Value( a ), new Value( b ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<Integer, String> get_custom_outfits(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_custom_outfits( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static String numberology_prize( final int a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.numberology_prize( interpreter, new Value( a ) );
		return result.toString();
	}

	public static Map<Integer, String> all_normal_outfits(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.all_normal_outfits( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static boolean black_market_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.black_market_available( interpreter );
		return result.intValue() != 0;
	}

	public static boolean is_online( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_online( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static String item_type( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_type( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static int item_amount( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.item_amount( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static int expected_damage( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.expected_damage( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<Integer, String> file_to_array( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.file_to_array( interpreter, new Value( a ) );
		return (Map<Integer, String>) result.rawValue();
	}

	public static int my_fury(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_fury( interpreter );
		return (int) result.intValue();
	}

	public static int my_fullness(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_fullness( interpreter );
		return (int) result.intValue();
	}

	public static boolean will_usually_dodge(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.will_usually_dodge( interpreter );
		return result.intValue() != 0;
	}

	public static boolean friars_available(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.friars_available( interpreter );
		return result.intValue() != 0;
	}

	public static int to_int( final boolean a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_int( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static String url_encode( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.url_encode( interpreter, new Value( a ) );
		return result.toString();
	}

	public static double numeric_modifier( final JSONObject a, final String b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.numeric_modifier( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ), new Value( b ) );
		return result.intValue();
	}

	public static boolean to_boolean( final boolean a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_boolean( interpreter, new Value( a ) );
		return result.intValue() != 0;
	}

	public static int last_choice(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.last_choice( interpreter );
		return (int) result.intValue();
	}

	public static int mall_price( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.mall_price( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static boolean put_display( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.put_display( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static StringBuffer run_turn(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.run_turn( interpreter );
		return (StringBuffer) result.rawValue();
	}

	public static Map<String, Integer> get_clan_rumpus(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_clan_rumpus( interpreter );
		return (Map<String, Integer>) result.rawValue();
	}

	public static StringBuffer load_html( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.load_html( interpreter, new Value( a ) );
		return (StringBuffer) result.rawValue();
	}

	public static JSONObject to_thrall( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_thrall( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static Map<JSONObject, Integer> my_effects(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.my_effects( interpreter );
		return (Map<JSONObject, Integer>) result.rawValue();
	}

	public static int to_int( final String a )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_int( interpreter, new Value( a ) );
		return (int) result.intValue();
	}

	public static Map<Integer, String> get_outfits(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_outfits( interpreter );
		return (Map<Integer, String>) result.rawValue();
	}

	public static int tavern(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.tavern( interpreter );
		return (int) result.intValue();
	}

	public static JSONObject to_familiar( final int a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_familiar( interpreter, new Value( a ) );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

	public static String substring( final String a, final int b, final int c )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.substring( interpreter, new Value( a ), new Value( b ), new Value( c ) );
		return result.toString();
	}

	public static double max( final double a, final double b )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.max( interpreter, new Value( a ), new Value( b ) );
		return result.intValue();
	}

	public static int monster_initiative(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_initiative( interpreter );
		return (int) result.intValue();
	}

	public static boolean in_multi_fight(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.in_multi_fight( interpreter );
		return result.intValue() != 0;
	}

	public static boolean is_goal( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.is_goal( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.intValue() != 0;
	}

	public static int to_slot( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.to_slot( interpreter, new Value( new Type( "item", 100 ), (int) a.get("id"), (String) a.get("name") ) );
		return (int) result.intValue();
	}

	public static Map<String, Integer> current_pvp_stances(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.current_pvp_stances( interpreter );
		return (Map<String, Integer>) result.rawValue();
	}

	public static String get_path(  )
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.get_path( interpreter );
		return result.toString();
	}

	public static int craft( final String a, final int b, final JSONObject c, final JSONObject d ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.craft( interpreter, new Value( a ), new Value( b ), new Value( new Type( "item", 100 ), (int) c.get("id"), (String) c.get("name") ), new Value( new Type( "item", 100 ), (int) d.get("id"), (String) d.get("name") ) );
		return (int) result.intValue();
	}

	public static String monster_manuel_text( final JSONObject a ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.monster_manuel_text( interpreter, new Value( new Type( "monster", 108 ), (int) a.get("id"), (String) a.get("name") ) );
		return result.toString();
	}

	public static boolean buy( final int a, final JSONObject b ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.buy( interpreter, new Value( a ), new Value( new Type( "item", 100 ), (int) b.get("id"), (String) b.get("name") ) );
		return result.intValue() != 0;
	}

	public static JSONObject daily_special(  ) throws JSONException
	{
		Interpreter interpreter = new Interpreter();
		Value result = RuntimeLibrary.daily_special( interpreter );
		return ((JSONObject) result.asProxy().toJSON()).put("id", (int) result.intValue());
	}

}
