import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.sql.*;


	public class DocomoDataProcess extends Thread
	{
	public static Connection con_source=null,con_destination=null;
	public static Statement stmt_source,stmt_destination;
	public String sIP=null,sDSN=null,sUSR=null,sPWD=null;
	public String dIP=null,dDSN=null,dUSR=null,dPWD=null;
	public static CallableStatement cstmt=null;
	public static int day=1;
	public static int i = 0;
	public void readDBCONFIG()
	{
		try
		{
			System.out.println("**********************************************************");
			System.out.println("**     Thread Started With The Following Configuration  **");
			System.out.println("**              File to be Read is DB.CFG          **");

			sIP="192.168.100.224";
			sDSN="master_db";
			sUSR="billing";
			sPWD="billing";

			dIP="192.168.100.224";
			dDSN="misdata";
			dUSR="billing";
			dPWD="billing";

			System.out.println("**SOURCE IP is  ["+sIP+"] **  DSN is ["+sDSN+"] Usr is ["+sUSR+"] Pwd is ["+sPWD+"]\t**");
			System.out.println("**DESTINATION IP is  ["+dIP+"] **  DSN is ["+dDSN+"] Usr is ["+dUSR+"] Pwd is ["+dPWD+"]\t**");
			System.out.println("**********************************************************");

		}
		catch(Exception e)
		{
			System.out.println("Exception while reading DB.cfg");
			e.printStackTrace();

		}
	}
	public DocomoDataProcess()
	{
		try
		{
			readDBCONFIG();
			System.out.println("Initiallizing DB");
			Class.forName("com.mysql.jdbc.Driver");
			con_source = DriverManager.getConnection("jdbc:mysql://"+sIP+"/"+sDSN, sUSR, sPWD);
			con_destination=DriverManager.getConnection("jdbc:mysql://"+dIP+"/"+dDSN, dUSR, dPWD);
			System.out.println("***Database Connection established!***");
			stmt_source = con_source.createStatement();
			stmt_destination = con_destination.createStatement();
			System.out.println("###DB CONNECTION UP FOR BOTH DB###");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/*******************MAKE TEST FILES********************/
		public void Fun_FileCreator(int Day)
		{
				try
				{
					CallableStatement cstmtfm1 = null;
					cstmtfm1 = con_destination.prepareCall("{call misdata.Docomo_Billing_Data_All(?)}");
					cstmtfm1.setInt(1,Day);
					cstmtfm1.execute();
					cstmtfm1.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
	/*******************MAKE TEST FILES END********************/

	public void run()
	{
		try{
			//	day=1;
				String FILEDATE = "";
				String BillingTable = "master_db.tbl_billing_success";
				String query_date = "select date_format(subdate(now(),"+day+"),'%Y%m%d') as 'FILEDATE'";
				if (day > 2)
					BillingTable = "master_db.tbl_billing_success_backup";
				else
					BillingTable = "master_db.tbl_billing_success";

				System.out.println("****Billing table is :****"+BillingTable);

				//ResultSet Rsdate = stmt_source.executeQuery(query_date);
				//while(Rsdate.next())
				//{
				//	FILEDATE = Rsdate.getString("FILEDATE");
				//	System.out.println("****Running for date :****"+FILEDATE);
				//}
				//System.out.println("****Running for date :****"+FILEDATE);

				String MU_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,if(status=1,'SUCCESS','FAILURE') 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1001";
				String MU_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1001";
				String MU_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(unsub_date),DCccg_ID from docomo_radio.tbl_radio_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";

				String Hun_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,if(status=1,'SUCCESS','FAILURE') 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1002 and sc not like ('%P%')";
				String Hun_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1002 and sc not like ('%P%')";
				String Hun_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(unsub_date),DCccg_ID from docomo_hungama.tbl_jbox_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";

				String FMJ_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,if(status=1,'SUCCESS','FAILURE') 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id='1005' and  Plan_id in(18,20,33,34)";
				String FMJFOLLOW_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,if(status=1,'SUCCESS','FAILURE') 'CHARGING REASON',response_time 'DATE',if(event_type='sub','Activation_Follow_5','Renewal_Follow_5') 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1005 and  Plan_id in(19)";
				String FMJ_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1005 and  Plan_id in(18,20,33,34)";
				String FMJFOLLOW_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',if(event_type='sub','Activation_Follow_5','Renewal_Follow_5') 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1005 and  Plan_id in(19)";
				String FMJ_unsub  = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(unsub_date),DCccg_ID from docomo_starclub.tbl_jbox_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";


				String RFM_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','RedFMTataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1010";
				String RFM_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','RedFMTataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1010";
				String RFM_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE','RedFMTataDoCoMo' SERVICE,date(unsub_date),DCccg_ID from docomo_redfm.tbl_jbox_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";

				String Riya_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1009 and  Plan_id not in(85,99,100,101)";
				String RiyaFOLLOW_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',response_time 'DATE',if(event_type='sub','Activation_Follow_5','Renewal_Follow_5') 'TYPE',pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id='1009' and  Plan_id in(85)";
				String RiyaEVENT_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',response_time 'DATE','EVENT' TYPE,pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id='1009' and  Plan_id in(99,100,101)";
				String Riya_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1009 and  Plan_id not in(85)";
				String RiyaFOLLOW_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',if(event_type='sub','Activation_Follow_5','Renewal_Follow_5') 'TYPE',pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id='1009' and  Plan_id in('85')";
				String Riya_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE','RIATataDoCoMo' SERVICE,date(unsub_date),DCccg_ID from docomo_manchala.tbl_riya_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";

				/*String MTV_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,if(status=1,'SUCCESS','FAILURE') 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','MTVTataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1003";
				String MTV_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE','MTVTataDoCoMo' SERVICE,date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1003";
				String MTV_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE','MTVTataDoCoMo' SERVICE,date(unsub_date),DCccg_ID from docomo_hungama.tbl_mtv_unsub nolock where date(unsub_date)=date(subdate(now(),"+day+"))";
*/
				String MND_Success = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from "+BillingTable+" nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1013";
				String MND_failure = "select concat('91',msisdn) 'MSISDN',if(mode is null,'IVR',mode) 'MODE','0' as 'CHARGING AMOUNT',circle,status as 'CHARGING REASON',response_time 'DATE',case event_type when 'sub' then 'Activation' when 'TOPUP' then 'TOP-UP' when 'EVENT' then 'EVENT' else 'Renewal' end 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(response_time),ccg_id,ifnull(aff_id,0) 'aff_id' from master_db.tbl_billing_failure nolock where date(response_time)=date(subdate(now(),"+day+")) and service_id=1013";
				String MND_unsub   = "select concat('91',ani) 'MSISDN',if(unsub_reason is null,'IVR',unsub_reason) 'MODE',if(chrg_amount is null,'0',chrg_amount) 'CHARGING AMOUNT',circle,'SUCCESS' as 'CHARGING REASON',unsub_date 'DATE','Deactivation' as 'TYPE',pre_post 'USER_TYPE',plan_id 'SERVICE',date(unsub_date),DCccg_ID from docomo_mnd.tbl_character_unsub1 nolock where date(unsub_date)=date(subdate(now(),"+day+")) and plan_id=106";


				String MSISDN = "";
				String MODE = "";
				String CHARGINGAMOUNT = "";
				String CIRCLE = "";
				String CHARGINGREASON = "";
				String DATE = "";
				String TYPE = "";
				String USER_TYPE = "";
				String SERVICE = "";
				String DATE1 = "";
				String TIME1 = "";
				String AFFID = "";

				ResultSet Rsdate = stmt_source.executeQuery(query_date);
				while(Rsdate.next())
				{
						FILEDATE = Rsdate.getString("FILEDATE");
						System.out.println("****Running for date :****"+FILEDATE);
				}
				stmt_destination.executeUpdate("truncate table tbl_billing_mu_tatadocomo");
				ResultSet Rsdnis = stmt_source.executeQuery(MU_Success);
				System.out.println("***********DOCOMO MU SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rsdnis.next())
				{
					try
					{

	            	    MSISDN = Rsdnis.getString("MSISDN");
	                    MODE = Rsdnis.getString("MODE");
	                    CHARGINGAMOUNT = Rsdnis.getString("CHARGING AMOUNT");
	                    CIRCLE = Rsdnis.getString("circle");
	                    CIRCLE = getCIRCLE(CIRCLE);
	                    CHARGINGREASON = Rsdnis.getString("CHARGING REASON");
	                    DATE = Rsdnis.getString("DATE");
	                    TYPE = Rsdnis.getString("TYPE");
	                    USER_TYPE = Rsdnis.getString("USER_TYPE");
	                    SERVICE = Rsdnis.getString("SERVICE");
	                    DATE1 = Rsdnis.getString("date(response_time)");
	                    TIME1 = Rsdnis.getString("ccg_id");
						AFFID = Rsdnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;

	                    //System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mu_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs2dnis = stmt_source.executeQuery(MU_failure);
				System.out.println("***********DOCOMO MU FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs2dnis.next())
				{
					try
					{
	            	    MSISDN = Rs2dnis.getString("MSISDN");
	                    MODE = Rs2dnis.getString("MODE");
	                    CHARGINGAMOUNT = Rs2dnis.getString("CHARGING AMOUNT");
	                    CIRCLE = Rs2dnis.getString("circle");
	                    CIRCLE = getCIRCLE(CIRCLE);
	                    CHARGINGREASON = Rs2dnis.getString("CHARGING REASON");
	                    DATE = Rs2dnis.getString("DATE");
	                    TYPE = Rs2dnis.getString("TYPE");
	                    USER_TYPE = Rs2dnis.getString("USER_TYPE");
	                    SERVICE = Rs2dnis.getString("SERVICE");
	                    DATE1 = Rs2dnis.getString("date(response_time)");
	                    TIME1 = Rs2dnis.getString("ccg_id");
						AFFID = Rs2dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
	                    //System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mu_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs3dnis = stmt_source.executeQuery(MU_unsub);
				System.out.println("***********DOCOMO MU UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs3dnis.next())
				{
					try
					{

	            	    MSISDN = Rs3dnis.getString("MSISDN");
	                    MODE = Rs3dnis.getString("MODE");
	                    CHARGINGAMOUNT = Rs3dnis.getString("CHARGING AMOUNT");
	                    CIRCLE = Rs3dnis.getString("circle");
	                    CIRCLE = getCIRCLE(CIRCLE);
	                    CHARGINGREASON = Rs3dnis.getString("CHARGING REASON");
	                    DATE = Rs3dnis.getString("DATE");
	                    TYPE = Rs3dnis.getString("TYPE");
	                    USER_TYPE = Rs3dnis.getString("USER_TYPE");
	                    SERVICE = Rs3dnis.getString("SERVICE");
	                    DATE1 = Rs3dnis.getString("date(unsub_date)");
	                    TIME1 = Rs3dnis.getString("DCccg_ID");
	                    //System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mu_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				stmt_destination.executeUpdate("truncate table tbl_billing_54646_tatadocomo");
	            ResultSet Rs4dnis = stmt_source.executeQuery(Hun_Success);
				System.out.println("***********DOCOMO 54646 SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs4dnis.next())
				{
					try
					{

						MSISDN = Rs4dnis.getString("MSISDN");
						MODE = Rs4dnis.getString("MODE");
						CHARGINGAMOUNT = Rs4dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs4dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs4dnis.getString("CHARGING REASON");
						DATE = Rs4dnis.getString("DATE");
						TYPE = Rs4dnis.getString("TYPE");
						USER_TYPE = Rs4dnis.getString("USER_TYPE");
						SERVICE = Rs4dnis.getString("SERVICE");
						DATE1 = Rs4dnis.getString("date(response_time)");
						TIME1 = Rs4dnis.getString("ccg_id");
						AFFID = Rs4dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;

						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_54646_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
	            ResultSet Rs5dnis = stmt_source.executeQuery(Hun_failure);
				System.out.println("***********DOCOMO 54646 FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs5dnis.next())
				{
					try
					{
						MSISDN = Rs5dnis.getString("MSISDN");
						MODE = Rs5dnis.getString("MODE");
						CHARGINGAMOUNT = Rs5dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs5dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs5dnis.getString("CHARGING REASON");
						DATE = Rs5dnis.getString("DATE");
						TYPE = Rs5dnis.getString("TYPE");
						USER_TYPE = Rs5dnis.getString("USER_TYPE");
						SERVICE = Rs5dnis.getString("SERVICE");
						DATE1 = Rs5dnis.getString("date(response_time)");
						TIME1 = Rs5dnis.getString("ccg_id");
						AFFID = Rs5dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_54646_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs6dnis = stmt_source.executeQuery(Hun_unsub);
				System.out.println("***********DOCOMO 54646 UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs6dnis.next())
				{
					try
					{
						MSISDN = Rs6dnis.getString("MSISDN");
						MODE = Rs6dnis.getString("MODE");
						CHARGINGAMOUNT = Rs6dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs6dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs6dnis.getString("CHARGING REASON");
						DATE = Rs6dnis.getString("DATE");
						TYPE = Rs6dnis.getString("TYPE");
						USER_TYPE = Rs6dnis.getString("USER_TYPE");
						SERVICE = Rs6dnis.getString("SERVICE");
						DATE1 = Rs6dnis.getString("date(unsub_date)");
						TIME1 = Rs6dnis.getString("DCccg_ID");

						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_54646_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				stmt_destination.executeUpdate("truncate table tbl_billing_fmj_tatadocomo");
				ResultSet Rs7dnis = stmt_source.executeQuery(FMJ_Success);
				System.out.println("***********DOCOMO FMJ_Success TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs7dnis.next())
				{
					try
					{
						MSISDN = Rs7dnis.getString("MSISDN");
						MODE = Rs7dnis.getString("MODE");
						CHARGINGAMOUNT = Rs7dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs7dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs7dnis.getString("CHARGING REASON");
						DATE = Rs7dnis.getString("DATE");
						TYPE = Rs7dnis.getString("TYPE");
						USER_TYPE = Rs7dnis.getString("USER_TYPE");
						SERVICE = Rs7dnis.getString("SERVICE");
						DATE1 = Rs7dnis.getString("date(response_time)");
						TIME1 = Rs7dnis.getString("ccg_id");
						AFFID = Rs7dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_fmj_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs8dnis = stmt_source.executeQuery(FMJFOLLOW_Success);
				System.out.println("***********DOCOMO FMJFOLLOW_Success TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs8dnis.next())
				{
					try
					{
						MSISDN = Rs8dnis.getString("MSISDN");
						MODE = Rs8dnis.getString("MODE");
						CHARGINGAMOUNT = Rs8dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs8dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs8dnis.getString("CHARGING REASON");
						DATE = Rs8dnis.getString("DATE");
						TYPE = Rs8dnis.getString("TYPE");
						USER_TYPE = Rs8dnis.getString("USER_TYPE");
						SERVICE = Rs8dnis.getString("SERVICE");
						DATE1 = Rs8dnis.getString("date(response_time)");
						TIME1 = Rs8dnis.getString("ccg_id");
						AFFID = Rs8dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;

						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_fmj_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs9dnis = stmt_source.executeQuery(FMJ_failure);
				System.out.println("***********DOCOMO FMJ FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs9dnis.next())
				{
					try
					{
						MSISDN = Rs9dnis.getString("MSISDN");
						MODE = Rs9dnis.getString("MODE");
						CHARGINGAMOUNT = Rs9dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs9dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs9dnis.getString("CHARGING REASON");
						DATE = Rs9dnis.getString("DATE");
						TYPE = Rs9dnis.getString("TYPE");
						USER_TYPE = Rs9dnis.getString("USER_TYPE");
						SERVICE = Rs9dnis.getString("SERVICE");
						DATE1 = Rs9dnis.getString("date(response_time)");
						TIME1 = Rs9dnis.getString("ccg_id");
						AFFID = Rs9dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_fmj_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs10dnis = stmt_source.executeQuery(FMJFOLLOW_failure);
				System.out.println("***********DOCOMO FMJFOLLOW FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs10dnis.next())
				{
					try
					{
						MSISDN = Rs10dnis.getString("MSISDN");
						MODE = Rs10dnis.getString("MODE");
						CHARGINGAMOUNT = Rs10dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs10dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs10dnis.getString("CHARGING REASON");
						DATE = Rs10dnis.getString("DATE");
						TYPE = Rs10dnis.getString("TYPE");
						USER_TYPE = Rs10dnis.getString("USER_TYPE");
						SERVICE = Rs10dnis.getString("SERVICE");
						DATE1 = Rs10dnis.getString("date(response_time)");
						TIME1 = Rs10dnis.getString("ccg_id");
						AFFID = Rs10dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_fmj_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs11dnis = stmt_source.executeQuery(FMJ_unsub);
				System.out.println("***********DOCOMO FMJ UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs11dnis.next())
				{
					try
					{
						MSISDN = Rs11dnis.getString("MSISDN");
						MODE = Rs11dnis.getString("MODE");
						CHARGINGAMOUNT = Rs11dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs11dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs11dnis.getString("CHARGING REASON");
						DATE = Rs11dnis.getString("DATE");
						TYPE = Rs11dnis.getString("TYPE");
						USER_TYPE = Rs11dnis.getString("USER_TYPE");
						SERVICE = Rs11dnis.getString("SERVICE");
						DATE1 = Rs11dnis.getString("date(unsub_date)");
						TIME1 = Rs11dnis.getString("DCccg_ID");
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_fmj_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				stmt_destination.executeUpdate("truncate table tbl_billing_redfm");
				ResultSet Rs13dnis = stmt_source.executeQuery(RFM_Success);
				System.out.println("***********DOCOMO RED FM SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs13dnis.next())
				{
					try
					{
						MSISDN = Rs13dnis.getString("MSISDN");
						MODE = Rs13dnis.getString("MODE");
						CHARGINGAMOUNT = Rs13dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs13dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs13dnis.getString("CHARGING REASON");
						DATE = Rs13dnis.getString("DATE");
						TYPE = Rs13dnis.getString("TYPE");
						USER_TYPE = Rs13dnis.getString("USER_TYPE");
						SERVICE = Rs13dnis.getString("SERVICE");
						DATE1 = Rs13dnis.getString("date(response_time)");
						TIME1 = Rs13dnis.getString("ccg_id");
						AFFID = Rs13dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;

						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_redfm values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs14dnis = stmt_source.executeQuery(RFM_failure);
				System.out.println("***********DOCOMO RED FM FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs14dnis.next())
				{
					try
					{
						MSISDN = Rs14dnis.getString("MSISDN");
						MODE = Rs14dnis.getString("MODE");
						CHARGINGAMOUNT = Rs14dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs14dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs14dnis.getString("CHARGING REASON");
						DATE = Rs14dnis.getString("DATE");
						TYPE = Rs14dnis.getString("TYPE");
						USER_TYPE = Rs14dnis.getString("USER_TYPE");
						SERVICE = Rs14dnis.getString("SERVICE");
						DATE1 = Rs14dnis.getString("date(response_time)");
						TIME1 = Rs14dnis.getString("ccg_id");
						AFFID = Rs14dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;

						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_redfm values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs15dnis = stmt_source.executeQuery(RFM_unsub);
				System.out.println("***********DOCOMO RED FM UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs15dnis.next())
				{
					try
					{
						MSISDN = Rs15dnis.getString("MSISDN");
						MODE = Rs15dnis.getString("MODE");
						CHARGINGAMOUNT = Rs15dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs15dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs15dnis.getString("CHARGING REASON");
						DATE = Rs15dnis.getString("DATE");
						TYPE = Rs15dnis.getString("TYPE");
						USER_TYPE = Rs15dnis.getString("USER_TYPE");
						SERVICE = Rs15dnis.getString("SERVICE");
						DATE1 = Rs15dnis.getString("date(unsub_date)");
						TIME1 = Rs15dnis.getString("DCccg_ID");
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_redfm values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				stmt_destination.executeUpdate("truncate table tbl_billing_ria");
				ResultSet Rs16dnis = stmt_source.executeQuery(Riya_Success);
				System.out.println("***********DOCOMO RIYA SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs16dnis.next())
				{
					try
					{
						MSISDN = Rs16dnis.getString("MSISDN");
						MODE = Rs16dnis.getString("MODE");
						CHARGINGAMOUNT = Rs16dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs16dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs16dnis.getString("CHARGING REASON");
						DATE = Rs16dnis.getString("DATE");
						TYPE = Rs16dnis.getString("TYPE");
						USER_TYPE = Rs16dnis.getString("USER_TYPE");
						SERVICE = Rs16dnis.getString("SERVICE");
						DATE1 = Rs16dnis.getString("date(response_time)");
						TIME1 = Rs16dnis.getString("ccg_id");
						AFFID = Rs16dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs66dnis = stmt_source.executeQuery(RiyaFOLLOW_Success);
				System.out.println("***********DOCOMO RIYA SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs66dnis.next())
				{
					try
					{
						MSISDN = Rs66dnis.getString("MSISDN");
						MODE = Rs66dnis.getString("MODE");
						CHARGINGAMOUNT = Rs66dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs66dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs66dnis.getString("CHARGING REASON");
						DATE = Rs66dnis.getString("DATE");
						TYPE = Rs66dnis.getString("TYPE");
						USER_TYPE = Rs66dnis.getString("USER_TYPE");
						SERVICE = Rs66dnis.getString("SERVICE");
						DATE1 = Rs66dnis.getString("date(response_time)");
						TIME1 = Rs66dnis.getString("ccg_id");
						AFFID = Rs66dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs666dnis = stmt_source.executeQuery(RiyaEVENT_Success);
				System.out.println("***********DOCOMO RIYA EVENT SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs666dnis.next())
				{
					try
					{
						MSISDN = Rs666dnis.getString("MSISDN");
						MODE = Rs666dnis.getString("MODE");
						CHARGINGAMOUNT = Rs666dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs666dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs666dnis.getString("CHARGING REASON");
						DATE = Rs666dnis.getString("DATE");
						TYPE = Rs666dnis.getString("TYPE");
						USER_TYPE = Rs666dnis.getString("USER_TYPE");
						SERVICE = Rs666dnis.getString("SERVICE");
						DATE1 = Rs666dnis.getString("date(response_time)");
						TIME1 = Rs666dnis.getString("ccg_id");
						AFFID = Rs666dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs17dnis = stmt_source.executeQuery(Riya_failure);
				System.out.println("***********DOCOMO RIYA FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs17dnis.next())
				{
					try
					{
						MSISDN = Rs17dnis.getString("MSISDN");
						MODE = Rs17dnis.getString("MODE");
						CHARGINGAMOUNT = Rs17dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs17dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs17dnis.getString("CHARGING REASON");
						DATE = Rs17dnis.getString("DATE");
						TYPE = Rs17dnis.getString("TYPE");
						USER_TYPE = Rs17dnis.getString("USER_TYPE");
						SERVICE = Rs17dnis.getString("SERVICE");
						DATE1 = Rs17dnis.getString("date(response_time)");
						TIME1 = Rs17dnis.getString("ccg_id");
						AFFID = Rs17dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs77dnis = stmt_source.executeQuery(RiyaFOLLOW_failure);
				System.out.println("***********DOCOMO RIYA FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs77dnis.next())
				{
					try
					{
						MSISDN = Rs77dnis.getString("MSISDN");
						MODE = Rs77dnis.getString("MODE");
						CHARGINGAMOUNT = Rs77dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs77dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs77dnis.getString("CHARGING REASON");
						DATE = Rs77dnis.getString("DATE");
						TYPE = Rs77dnis.getString("TYPE");
						USER_TYPE = Rs77dnis.getString("USER_TYPE");
						SERVICE = Rs77dnis.getString("SERVICE");
						DATE1 = Rs77dnis.getString("date(response_time)");
						TIME1 = Rs77dnis.getString("ccg_id");
						AFFID = Rs77dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs18dnis = stmt_source.executeQuery(Riya_unsub);
				System.out.println("***********DOCOMO RIYA UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs18dnis.next())
				{
					try
					{
						MSISDN = Rs18dnis.getString("MSISDN");
						MODE = Rs18dnis.getString("MODE");
						CHARGINGAMOUNT = Rs18dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs18dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs18dnis.getString("CHARGING REASON");
						DATE = Rs18dnis.getString("DATE");
						TYPE = Rs18dnis.getString("TYPE");
						USER_TYPE = Rs18dnis.getString("USER_TYPE");
						SERVICE = Rs18dnis.getString("SERVICE");
						DATE1 = Rs18dnis.getString("date(unsub_date)");
						TIME1 = Rs18dnis.getString("DCccg_ID");
					//	AFFID = Rs18dnis.getString("aff_id");
					//	if(MODE.startsWith("WAP"))
					//		MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_ria values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
	           /* String str6 = "select count(1) as CNT from tbl_billing_mtv where DATE1 = date(subdate(now()," + day + ")) and service='MTVTatadocomo'";
				ResultSet localResultSet6 = stmt_destination.executeQuery(str6);
				if (localResultSet6.next())
				{
					i = localResultSet6.getInt("CNT");
					System.out.println("ROW COUNT IS= " + i);
					localResultSet6.close();
					if (i >= 1)
					{
					  System.out.println("****Deleting the records****");
					  stmt_destination.executeUpdate("delete from tbl_billing_mtv where DATE1 = date(subdate(now()," + day + ")) and service='MTVTatadocomo'");
					}
					else
					{
					   System.out.println("****No DB Records to Delete****");
					}
				}
				ResultSet Rs19dnis = stmt_source.executeQuery(MTV_Success);
				System.out.println("***********DOCOMO MTV SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs19dnis.next())
				{
					try
					{
						MSISDN = Rs19dnis.getString("MSISDN");
						MODE = Rs19dnis.getString("MODE");
						CHARGINGAMOUNT = Rs19dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs19dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs19dnis.getString("CHARGING REASON");
						DATE = Rs19dnis.getString("DATE");
						TYPE = Rs19dnis.getString("TYPE");
						USER_TYPE = Rs19dnis.getString("USER_TYPE");
						SERVICE = Rs19dnis.getString("SERVICE");
						DATE1 = Rs19dnis.getString("date(response_time)");
						TIME1 = Rs19dnis.getString("ccg_id");
						AFFID = Rs19dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mtv values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs20dnis = stmt_source.executeQuery(MTV_failure);
				System.out.println("***********DOCOMO MTV FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs20dnis.next())
				{
					try
					{
						MSISDN = Rs20dnis.getString("MSISDN");
						MODE = Rs20dnis.getString("MODE");
						CHARGINGAMOUNT = Rs20dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs20dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs20dnis.getString("CHARGING REASON");
						DATE = Rs20dnis.getString("DATE");
						TYPE = Rs20dnis.getString("TYPE");
						USER_TYPE = Rs20dnis.getString("USER_TYPE");
						SERVICE = Rs20dnis.getString("SERVICE");
						DATE1 = Rs20dnis.getString("date(response_time)");
						TIME1 = Rs20dnis.getString("ccg_id");
						AFFID = Rs20dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mtv values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs21dnis = stmt_source.executeQuery(MTV_unsub);
				System.out.println("***********DOCOMO MTV UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs21dnis.next())
				{
					try
					{
						MSISDN = Rs21dnis.getString("MSISDN");
						MODE = Rs21dnis.getString("MODE");
						CHARGINGAMOUNT = Rs21dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs21dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs21dnis.getString("CHARGING REASON");
						DATE = Rs21dnis.getString("DATE");
						TYPE = Rs21dnis.getString("TYPE");
						USER_TYPE = Rs21dnis.getString("USER_TYPE");
						SERVICE = Rs21dnis.getString("SERVICE");
						DATE1 = Rs21dnis.getString("date(unsub_date)");
						TIME1 = Rs21dnis.getString("DCccg_ID");
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mtv values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }*/
				stmt_destination.executeUpdate("truncate table tbl_billing_mnd_tatadocomo");
				ResultSet Rs22dnis = stmt_source.executeQuery(MND_Success);
				System.out.println("***********DOCOMO MND SUCCESS TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs22dnis.next())
				{
					try
					{
						MSISDN = Rs22dnis.getString("MSISDN");
						MODE = Rs22dnis.getString("MODE");
						CHARGINGAMOUNT = Rs22dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs22dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs22dnis.getString("CHARGING REASON");
						DATE = Rs22dnis.getString("DATE");
						TYPE = Rs22dnis.getString("TYPE");
						USER_TYPE = Rs22dnis.getString("USER_TYPE");
						SERVICE = Rs22dnis.getString("SERVICE");
						DATE1 = Rs22dnis.getString("date(response_time)");
						TIME1 = Rs22dnis.getString("ccg_id");
						AFFID = Rs22dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mnd_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs23dnis = stmt_source.executeQuery(MND_failure);
				System.out.println("***********DOCOMO MND FAILURE TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs23dnis.next())
				{
					try
					{
						MSISDN = Rs23dnis.getString("MSISDN");
						MODE = Rs23dnis.getString("MODE");
						CHARGINGAMOUNT = Rs23dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs23dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs23dnis.getString("CHARGING REASON");
						DATE = Rs23dnis.getString("DATE");
						TYPE = Rs23dnis.getString("TYPE");
						USER_TYPE = Rs23dnis.getString("USER_TYPE");
						SERVICE = Rs23dnis.getString("SERVICE");
						DATE1 = Rs23dnis.getString("date(response_time)");
						TIME1 = Rs23dnis.getString("ccg_id");
						AFFID = Rs23dnis.getString("aff_id");
						if(MODE.startsWith("WAP"))
							MODE=MODE+"_"+AFFID;
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mnd_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				ResultSet Rs24dnis = stmt_source.executeQuery(MND_unsub);
				System.out.println("***********DOCOMO MND UNSUB TABLE DATA INSERTING AT DESTINATION***********");
				while(Rs24dnis.next())
				{
					try
					{
						MSISDN = Rs24dnis.getString("MSISDN");
						MODE = Rs24dnis.getString("MODE");
						CHARGINGAMOUNT = Rs24dnis.getString("CHARGING AMOUNT");
						CIRCLE = Rs24dnis.getString("circle");
						CIRCLE = getCIRCLE(CIRCLE);
						CHARGINGREASON = Rs24dnis.getString("CHARGING REASON");
						DATE = Rs24dnis.getString("DATE");
						TYPE = Rs24dnis.getString("TYPE");
						USER_TYPE = Rs24dnis.getString("USER_TYPE");
						SERVICE = Rs24dnis.getString("SERVICE");
						DATE1 = Rs24dnis.getString("date(unsub_date)");
						TIME1 = Rs24dnis.getString("DCccg_ID");
						//System.out.println(MSISDN+","+MODE+","+CHARGINGAMOUNT+","+CIRCLE+","+CHARGINGREASON+","+DATE+","+TYPE+","+USER_TYPE+","+SERVICE+","+DATE1+","+TIME1);
						stmt_destination.executeUpdate("insert into tbl_billing_mnd_tatadocomo values('"+MSISDN+"','"+MODE+"','"+CHARGINGAMOUNT+"','"+CIRCLE+"','"+CHARGINGREASON+"','"+DATE+"','"+TYPE+"','"+USER_TYPE+"','"+SERVICE+"','"+DATE1+"','"+TIME1+"')");
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
	            }
				stmt_destination.executeUpdate("update tbl_billing_mu_tatadocomo set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mu_tatadocomo set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mu_tatadocomo set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				stmt_destination.executeUpdate("update tbl_billing_mtv set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mtv set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mtv set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				stmt_destination.executeUpdate("update tbl_billing_ria set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+")) and service='RiaTatadocomo'");
				stmt_destination.executeUpdate("update tbl_billing_ria set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+")) and service='RiaTatadocomo'");
				stmt_destination.executeUpdate("update tbl_billing_ria set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+")) and service='RiaTatadocomo'");

				stmt_destination.executeUpdate("update tbl_billing_redfm set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_redfm set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_redfm set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				stmt_destination.executeUpdate("update tbl_billing_fmj_tatadocomo set type='Activation_Ticket' where service='20' and type='Activation' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_fmj_tatadocomo set type='Renewal_Ticket' where service='20' and type='Renewal' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_fmj_tatadocomo set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_fmj_tatadocomo set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_fmj_tatadocomo set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				stmt_destination.executeUpdate("update tbl_billing_54646_tatadocomo set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_54646_tatadocomo set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_54646_tatadocomo set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				stmt_destination.executeUpdate("update tbl_billing_mnd_tatadocomo set mode='IVR' where mode REGEXP '^-?[0-9]+$' and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mnd_tatadocomo set `CHARGING REASON`='33' where  (`CHARGING REASON` is NULL or`CHARGING REASON`='NA') and DATE1 = date(subdate(now(),"+day+"))");
				stmt_destination.executeUpdate("update tbl_billing_mnd_tatadocomo set `USER TYPE`='NA' where  (`USER TYPE` in ('0','/N','NULL') or `USER TYPE` is NULL) and DATE1 = date(subdate(now(),"+day+"))");

				System.out.println("************************Updations done closing connections**********************************");

				Thread.sleep(1000);

				Fun_FileCreator(day);

				Thread.sleep(1000);

				stmt_source.close();
				stmt_destination.close();
				con_source.close();
				con_destination.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
			        // The finally clause is always executed - even in error
			        // conditions PreparedStatements and Connections will always be closed
			        try
			        {
			        	if (con_source != null)
			            	con_source.close();
			        }
			        catch(Exception e) {}

			        try
			        {
			        	if (con_destination != null)
			            	con_destination.close();
			        }
			        catch (Exception e){}
			  }
	}
	public String getCIRCLE(String CIR)
	{
		String c;
		if("APD".equalsIgnoreCase(CIR))
			c = "Andhra Pradesh";
		else if("ASM".equalsIgnoreCase(CIR))
			c= "Assam";
		else if("BIH".equalsIgnoreCase(CIR))
			c= "Bihar";
		else if("CHN".equalsIgnoreCase(CIR))
			c = "Chennai";
		else if("DEL".equalsIgnoreCase(CIR))
			c = "Delhi";
		else if("GUJ".equalsIgnoreCase(CIR))
			c = "Gujarat";
		else if("HAY".equalsIgnoreCase(CIR)|| "HAR".equalsIgnoreCase(CIR))
			c = "Haryana";
		else if("HPD".equalsIgnoreCase(CIR))
			c = "Himachal Pradesh";
		else if("JNK".equalsIgnoreCase(CIR))
			c = "Jammu-Kashmir";
		else if("KAR".equalsIgnoreCase(CIR))
			c = "Karnataka";
		else if("KER".equalsIgnoreCase(CIR))
			c = "Kerala";
		else if("KOL".equalsIgnoreCase(CIR))
			c = "Kolkata";
		else if("MPD".equalsIgnoreCase(CIR))
			c = "Madhya Pradesh";
		else if("MAH".equalsIgnoreCase(CIR))
			c = "Maharashtra";
		else if("MUM".equalsIgnoreCase(CIR))
			c = "Mumbai";
		else if("NES".equalsIgnoreCase(CIR))
			c = "NE";
		else if("ORI".equalsIgnoreCase(CIR))
			c = "Orissa";
		else if("PUB".equalsIgnoreCase(CIR)|| "PUN".equalsIgnoreCase(CIR))
			c = "Punjab";
		else if("RAJ".equalsIgnoreCase(CIR))
			c = "Rajasthan";
		else if("TNU".equalsIgnoreCase(CIR))
			c = "Tamil Nadu";
		else if("UPE".equalsIgnoreCase(CIR))
			c = "UP EAST";
		else if("UPW".equalsIgnoreCase(CIR))
			c = "UP WEST";
		else if("WBL".equalsIgnoreCase(CIR))
			c = "WestBengal";
		else
			c = "Others";
		return c;
	}
	public static void main(String arg[])
	{
		if(arg.length>0)
			day = Integer.parseInt(arg[0]);
		else
			day = 1;
		DocomoDataProcess c = new DocomoDataProcess();
		c.start();

	}

}
