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
import java.util.*;
import java.io.*;

public class DigiDataValidation extends Thread
{
	public static Connection con_source=null,con_destination=null,con_Tunetalk=null;
	public static Statement stmt_source,stmt_destination,stmt_tunetalk;
	public String sIP=null,sDSN=null,sUSR=null,sPWD=null;
	public String tIP=null,tDSN=null,tUSR=null,tPWD=null;
	public String dIP=null,dDSN=null,dUSR=null,dPWD=null;
	public static CallableStatement cstmt=null;
	public static int day=1;
	public static int cnt1 = 0;
	public static int cnt2 = 0;
	public static int cnt3 = 0;
	public static int cnt4 = 0;
	public static int cnt5 = 0;
	public static int cnt6 = 0;
	public static int cnt7 = 0;
	public static int cnt8 = 0;
	public static int cnt9 = 0;
	public static int sum1 = 0;

	public void readDBCONFIG()
	{
		try
		{
			System.out.println("**********************************************************");
			System.out.println("**     Thread Started With The Following Configuration  **");
			System.out.println("**              File to be Read is DB.CFG          **");

		/*	tIP="10.151.41.83";
			tDSN="master_db";
			tUSR="Manish_dev";
			tPWD="Man@123";*/

			sIP="172.16.56.42";
			sDSN="master_db";
			sUSR="billing";
			sPWD="billing";

			/*dIP="119.82.69.218";
			dDSN="misdata";
			dUSR="kunalk.arora";
			dPWD="google";*/
			dIP="192.168.100.218";
			dDSN="misdata";
			dUSR="billing";
			dPWD="billing";

			System.out.println("**SOURCE IP is  ["+sIP+"] **  DSN is ["+sDSN+"] Usr is ["+sUSR+"] Pwd is ["+sPWD+"]\t**");
			System.out.println("**DESTINATION IP is  ["+dIP+"] **  DSN is ["+dDSN+"] Usr is ["+dUSR+"] Pwd is ["+dPWD+"]\t**");
		//	System.out.println("**TUNETALK IP is  ["+tIP+"] **  DSN is ["+tDSN+"] Usr is ["+tUSR+"] Pwd is ["+tPWD+"]\t**");
			System.out.println("**********************************************************");

		}
		catch(Exception e)
		{
			System.out.println("Exception while reading DB.cfg");
			e.printStackTrace();

		}
	}

	public DigiDataValidation()
	{
		try
		{
			readDBCONFIG();
			System.out.println("Initiallizing DB");
			Class.forName("com.mysql.jdbc.Driver");
			con_source = DriverManager.getConnection("jdbc:mysql://"+sIP+"/"+sDSN, sUSR, sPWD);
			//con_Tunetalk = DriverManager.getConnection("jdbc:mysql://"+tIP+"/"+tDSN, tUSR, tPWD);
			con_destination=DriverManager.getConnection("jdbc:mysql://"+dIP+"/"+dDSN, dUSR, dPWD);
			System.out.println("***Database Connection established!***");
			stmt_source = con_source.createStatement();
			//stmt_tunetalk = con_Tunetalk.createStatement();
			stmt_destination = con_destination.createStatement();
			System.out.println("###DB CONNECTION UP FOR BOTH DB###");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String arg[])
	{
		if(arg.length>0)
			day = Integer.parseInt(arg[0]);
		else
			day = 1;
		DigiDataValidation c = new DigiDataValidation();
		c.start();
	}

	public void run()
	{
		try
		{
			//day=1;
			DigiBenValid();
			DigiIndValid();
			DigiNepValid();
			//TunetalkValid();
		}
		catch(Exception ex){PrintAndLog("Exception in run method "+ex.toString());}
		finally
		{
			// The finally clause is always executed - even in error
			// conditions PreparedStatements and Connections will always be closed
			try
			{
				stmt_source.close();
				if (con_source != null)
					con_source.close();
			}
			catch(Exception e) {}
			try
			{
				stmt_destination.close();
				if (con_destination != null)
					con_destination.close();
			}
			catch (Exception e){}
			try
			{
				stmt_tunetalk.close();
				if (con_Tunetalk != null)
					con_Tunetalk.close();
			}
			catch (Exception e){}
		}
	}
	public int DigiBenValid()
	{
		try
		{
				String str1 = "select count(1) as CNT1 from tbl_billing_mod_digi nolock where DATE1 = date(subdate(now(),"+day+")) and service in('2','5')";
				ResultSet localResultSet1 = stmt_destination.executeQuery(str1);
				if(localResultSet1.next())
				{
					cnt1 = localResultSet1.getInt("CNT1");
					System.out.println("DigiBen Destination table count is= " + cnt1);
					localResultSet1.close();
				}
	            String str2 = "select count(1) as CNT2 from master_db.tbl_billing_success nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(2,5)";
				ResultSet localResultSet2 = stmt_source.executeQuery(str2);
				if (localResultSet2.next())
				{
					cnt2 = localResultSet2.getInt("CNT2");
					System.out.println("DigiBen Success table count is= " + cnt2);
					localResultSet2.close();
				}
	            String str3 = "select count(1) as CNT3 from master_db.tbl_billing_failure nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(2,5)";
				ResultSet localResultSet3 = stmt_source.executeQuery(str3);
				if (localResultSet3.next())
				{
					cnt3 = localResultSet3.getInt("CNT3");
					System.out.println("DigiBen Failure table count is= " + cnt3);
					localResultSet3.close();
				}
	            String str4 = "select count(1) as CNT4 from dm_radio_bengali.tbl_digi_unsub nolock where date(unsub_date) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet4 = stmt_source.executeQuery(str4);
				if (localResultSet4.next())
				{
					cnt4 = localResultSet4.getInt("CNT4");
					System.out.println("DigiBen Unsub table count is= " + cnt4);
					localResultSet4.close();
				}
				sum1= cnt2 + cnt3 + cnt4;
				cnt5 = 	sum1 - cnt1;

				System.out.println("****Deleting old entry from validation table for VMI MU****");
				stmt_source.executeUpdate("delete from dm_radio.tbl_DIGIData_Validation where date(date_time) = date(subdate(now(),"+day+")) and service_name='DigiBen'");

				System.out.println("****Inserting into validation table****");
				stmt_source.executeUpdate("insert into dm_radio.tbl_DIGIData_Validation values('DigiBen',subdate(now(),"+day+"),'"+sum1+"','"+cnt1+"','"+cnt5+"')");
				System.out.println("********************************************************************************************************");
				System.out.println("************DigiBen count at source DB**** "+sum1+"******************************************************");
				System.out.println("************DigiBen count at Destination DB**** "+cnt1+"*************************************************");

				if(cnt5 > 100)
					System.out.println("****DigiBen count Difference at Both DB**** "+cnt5+"**********************************************");
				else
					System.out.println("****DigiBen Validation is fine,difference is**** "+cnt5+"*****************************************");
				System.out.println("********************************************************************************************************");
				cnt1 = 0;cnt2 = 0;cnt3 = 0;cnt4 = 0;cnt5 = 0;sum1 = 0;
		}
		catch(Exception DigiBenValid)
		{
				PrintAndLog("Exception in DigiBenValid "+DigiBenValid.toString()+".");
		}
		return 0;
	}

	public int DigiIndValid()
	{
		try
		{
				String str1 = "select count(1) as CNT1 from tbl_billing_mod_digi nolock where DATE1 = date(subdate(now()," + day + ")) and service in('1','4')";
				ResultSet localResultSet1 = stmt_destination.executeQuery(str1);
				if(localResultSet1.next())
				{
					cnt1 = localResultSet1.getInt("CNT1");
					System.out.println("DigiInd Destination table count is= " + cnt1);
					localResultSet1.close();
				}
	            String str2 = "select count(1) as CNT2 from master_db.tbl_billing_success nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(1,4)";
				ResultSet localResultSet2 = stmt_source.executeQuery(str2);
				if (localResultSet2.next())
				{
					cnt2 = localResultSet2.getInt("CNT2");
					System.out.println("DigiInd Success table count is= " + cnt2);
					localResultSet2.close();
				}
	            String str3 = "select count(1) as CNT3 from master_db.tbl_billing_failure nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(1,4)";
				ResultSet localResultSet3 = stmt_source.executeQuery(str3);
				if (localResultSet3.next())
				{
					cnt3 = localResultSet3.getInt("CNT3");
					System.out.println("DigiInd Failure table count is= " + cnt3);
					localResultSet3.close();
				}
	            String str4 = "select count(1) as CNT4 from dm_radio.tbl_digi_unsub nolock where date(unsub_date) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet4 = stmt_source.executeQuery(str4);
				if (localResultSet4.next())
				{
					cnt4 = localResultSet4.getInt("CNT4");
					System.out.println("DigiInd Unsub table count is= " + cnt4);
					localResultSet4.close();
				}
				sum1= cnt2 + cnt3 + cnt4;
				cnt5 = 	sum1 - cnt1;

				System.out.println("****Deleting old entry from validation table for Docomo Redfm****");
				stmt_source.executeUpdate("delete from dm_radio.tbl_DIGIData_Validation where date(date_time) = date(subdate(now(),"+day+")) and service_name='DigiInd'");
				System.out.println("****Inserting into validation table****");
				stmt_source.executeUpdate("insert into dm_radio.tbl_DIGIData_Validation values('DigiInd',subdate(now(),"+day+"),'"+sum1+"','"+cnt1+"','"+cnt5+"')");
				System.out.println("********************************************************************************************************");
				System.out.println("************DigiInd count at source DB**** "+sum1+"******************************************************");
				System.out.println("************DigiInd count at Destination DB**** "+cnt1+"*************************************************");

				if(cnt5 > 100)
					System.out.println("****DigiInd count Difference at Both DB**** "+cnt5+"**********************************************");
				else
					System.out.println("****DigiInd Validation is fine,difference is**** "+cnt5+"*****************************************");
				System.out.println("********************************************************************************************************");
				cnt1 = 0;cnt2 = 0;cnt3 = 0;cnt4 = 0;cnt5 = 0;sum1 = 0;
		}
		catch(Exception DigiIndValid)
		{
				PrintAndLog("Exception in DigiIndValid "+DigiIndValid.toString()+".");
		}
		return 0;
	}
	public int DigiNepValid()
	{
		try
		{
				String str1 = "select count(1) as CNT1 from tbl_billing_mod_digi nolock where DATE1 = date(subdate(now()," + day + ")) and service in(3,6)";
				ResultSet localResultSet1 = stmt_destination.executeQuery(str1);
				if(localResultSet1.next())
				{
					cnt1 = localResultSet1.getInt("CNT1");
					System.out.println("DigiNep Destination table count is= " + cnt1);
					localResultSet1.close();
				}
	            String str2 = "select count(1) as CNT2 from master_db.tbl_billing_success nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(3,6)";
				ResultSet localResultSet2 = stmt_source.executeQuery(str2);
				if (localResultSet2.next())
				{
					cnt2 = localResultSet2.getInt("CNT2");
					System.out.println("DigiNep Success table count is= " + cnt2);
					localResultSet2.close();
				}
	            String str3 = "select count(1) as CNT3 from master_db.tbl_billing_failure nolock where date(response_time) = date(subdate(now(),"+day+")) and plan_id in(3,6)";
				ResultSet localResultSet3 = stmt_source.executeQuery(str3);
				if (localResultSet3.next())
				{
					cnt3 = localResultSet3.getInt("CNT3");
					System.out.println("DigiNep Failure table count is= " + cnt3);
					localResultSet3.close();
				}
	            String str4 = "select count(1) as CNT4 from dm_radio_nepali.tbl_digi_unsub nolock where date(unsub_date) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet4 = stmt_source.executeQuery(str4);
				if (localResultSet4.next())
				{
					cnt4 = localResultSet4.getInt("CNT4");
					System.out.println("DigiNep Unsub table count is= " + cnt4);
					localResultSet4.close();
				}
				sum1= cnt2 + cnt3 + cnt4;
				cnt5 = 	sum1 - cnt1;

				System.out.println("****Deleting old entry from validation table for DigiNep****");
				stmt_source.executeUpdate("delete from dm_radio.tbl_DIGIData_Validation where date(date_time) = date(subdate(now(),"+day+")) and service_name='DigiNep'");
				System.out.println("****Inserting into validation table****");
				stmt_source.executeUpdate("insert into dm_radio.tbl_DIGIData_Validation values('DigiNep',subdate(now(),"+day+"),'"+sum1+"','"+cnt1+"','"+cnt5+"')");
				System.out.println("********************************************************************************************************");
				System.out.println("************DigiNep count at source DB**** "+sum1+"******************************************************");
				System.out.println("************DigiNep count at Destination DB**** "+cnt1+"*************************************************");

				if(cnt5 > 100)
					System.out.println("****DigiNep count Difference at Both DB**** "+cnt5+"**********************************************");
				else
					System.out.println("****DigiNep Validation is fine,difference is**** "+cnt5+"*****************************************");
				System.out.println("********************************************************************************************************");
				cnt1 = 0;cnt2 = 0;cnt3 = 0;cnt4 = 0;cnt5 = 0;sum1 = 0;
		}
		catch(Exception DigiNepValid)
		{
				PrintAndLog("Exception in DigiNepValid "+DigiNepValid.toString()+".");
		}
		return 0;
	}
	public int TunetalkValid()
	{
		try
		{
				String str1 = "select count(1) as CNT1 from tbl_billing_mod_tunetalk nolock where DATE1 = date(subdate(now()," + day + "))";
				ResultSet localResultSet1 = stmt_destination.executeQuery(str1);
				if(localResultSet1.next())
				{
					cnt1 = localResultSet1.getInt("CNT1");
					System.out.println("Tunetalk Destination table count is= " + cnt1);
					localResultSet1.close();
				}
	            String str2 = "select count(1) as CNT2 from master_db.tbl_billing_success nolock where date(response_time) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet2 = stmt_tunetalk.executeQuery(str2);
				if (localResultSet2.next())
				{
					cnt2 = localResultSet2.getInt("CNT2");
					System.out.println("Tunetalk Success table count is= " + cnt2);
					localResultSet2.close();
				}
	            String str3 = "select count(1) as CNT3 from master_db.tbl_billing_failure nolock where date(response_time) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet3 = stmt_tunetalk.executeQuery(str3);
				if (localResultSet3.next())
				{
					cnt3 = localResultSet3.getInt("CNT3");
					System.out.println("Tunetalk Failure table count is= " + cnt3);
					localResultSet3.close();
				}
	            String str4 = "select count(1) as CNT4 from tunetalk_radio.tbl_tunetalk_unsub nolock where date(unsub_date) = date(subdate(now(),"+day+"))";
				ResultSet localResultSet4 = stmt_tunetalk.executeQuery(str4);
				if (localResultSet4.next())
				{
					cnt4 = localResultSet4.getInt("CNT4");
					System.out.println("Tunetalk Unsub table count is= " + cnt4);
					localResultSet4.close();
				}
				sum1= cnt2 + cnt3 + cnt4;
				cnt5 = 	sum1 - cnt1;

				System.out.println("****Deleting old entry from validation table for Tunetalk****");
				stmt_source.executeUpdate("delete from dm_radio.tbl_DIGIData_Validation where date(date_time) = date(subdate(now(),"+day+")) and service_name='Tunetalk'");
				System.out.println("****Inserting into validation table****");
				stmt_source.executeUpdate("insert into dm_radio.tbl_DIGIData_Validation values('Tunetalk',subdate(now(),"+day+"),'"+sum1+"','"+cnt1+"','"+cnt5+"')");
				System.out.println("********************************************************************************************************");
				System.out.println("************Tunetalk count at source DB**** "+sum1+"******************************************************");
				System.out.println("************Tunetalk count at Destination DB**** "+cnt1+"*************************************************");

				if(cnt5 > 100)
					System.out.println("****Tunetalk count Difference at Both DB**** "+cnt5+"**********************************************");
				else
					System.out.println("****Tunetalk Validation is fine,difference is**** "+cnt5+"*****************************************");
				System.out.println("********************************************************************************************************");
				cnt1 = 0;cnt2 = 0;cnt3 = 0;cnt4 = 0;cnt5 = 0;sum1 = 0;
		}
		catch(Exception TunetalkValid)
		{
				PrintAndLog("Exception in TunetalkValid "+TunetalkValid.toString()+".");
		}
		return 0;
	}
	/*************************************************************************/
	public int PrintAndLog(String Buff)
	{
		try
		{
			Calendar today 	= Calendar.getInstance();
			String FileName	= "DATA_VALIDATION";
			String strlogfile = ""+formatN(""+today.get(Calendar.YEAR),4) + formatN(""+(today.get(Calendar.MONTH)+1),2) + formatN(""+today.get(Calendar.DATE),2);
			String strdate = formatN(""+today.get(Calendar.YEAR),4) + formatN(""+(today.get(Calendar.MONTH)+1),2) + formatN(""+today.get(Calendar.DATE),2);
			String strtime = formatN(""+today.get(Calendar.HOUR_OF_DAY),2)+formatN(""+today.get(Calendar.MINUTE),2)+formatN(""+today.get(Calendar.SECOND),2);
			Buff = "["+FileName+" "+strdate+" "+strtime +"]--> "+Buff;
			System.out.println(Buff);
			FileOutputStream outfile = new FileOutputStream("./log/" +FileName+"_"+ strlogfile + ".log",true);
			PrintStream outprint = new PrintStream(outfile);
			outprint.println(Buff);
			outprint.close();
			outfile.close();
			return 1;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return 0;
		}
	}
	/*********************************************************************************/
	public String formatN(String str, int x)
	{
		int len;
		String ret_str="";
		len = str.length();
		if (len >= x)
			ret_str = str;
		else
		{
			for(int i=0; i<x-len; i++)
				ret_str = ret_str + "0";
			ret_str = ret_str + str;
		}
		return ret_str;
	}


}
