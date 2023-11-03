import java.sql.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
class BankSession{
	Statement st;
	int balance;
	String uname;
	boolean LoggedIn = false;
	Statement connect(){
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/BankOfCA","root","root");
			//System.out.println("Connection Successful");
			return con.createStatement();
		}
		catch(Exception e){ System.out.println(e);} 
		return null;
	}

	String sha(String password){
		try{
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] hashBytes = sha1.digest(password.getBytes());
			StringBuilder hexString = new StringBuilder();
			for(byte b: hashBytes){
				String hex = Integer.toHexString(0xFF&b);
				if(hex.length()==1){
					hexString.append("0");
				}
				hexString.append(hex);
			}
			String sha1Hash = hexString.toString();
			return sha1Hash;
		}
		catch(Exception e){
			System.out.println(e);
		}
		return "";
	}

	void createAccount(){
		Scanner sc = new Scanner(System.in);
		String username, password, address, contact, adhaar;
		int DOBday,DOBmonth,DOByear;
		System.out.println("Enter username : ");
		username=sc.next();
		System.out.println("Enter password : ");
		password=sc.next();
		sc.nextLine();
		System.out.println("Enter address : ");
		address=sc.nextLine();
		System.out.println("AD: "+address);
		System.out.println("Enter Adhaar Number : ");
		adhaar=sc.next();
		System.out.println("Enter mobile number : ");
		contact = sc.next();
		while(password.length()<8){
			System.out.println("Password is too small, enter again : ");
			password = sc.next();
		}
		try{
				String sha1Hash = sha(password);
				String query = String.format("INSERT INTO Customers (Username,Pass,Address,Adhaar,Contact) VALUES (\"%s\",\"%s\",\"%s\",\"%s\",\"%s\")",username,sha1Hash,address,adhaar,contact);
				System.out.println(query);
				st=connect();
				st.executeUpdate(query);
				query = String.format("INSERT INTO accounts (Username, Balance) VALUES (\"%s\",%s)",username,0);
				System.out.println(query);
				st.executeUpdate(query);
				System.out.println("Account added");
		} catch(Exception e){
			System.out.println(e);
		}
	}

	void login(){
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/BankOfCA","root","root");
			//System.out.println("Connection Successful");
			st=connect();
			//con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch(Exception e){ System.out.println(e);} 
		Scanner sc = new Scanner(System.in);
		String user,pass;
		System.out.println("ENTER USER : ");
		user=sc.next();
		System.out.println("ENTER PASS : ");
		pass=sc.next();
		try{
			ResultSet rs = st.executeQuery("SELECT * FROM Customers WHERE Username = \""+user+"\" AND Pass ="+"\""+sha(pass)+"\"");
			//System.out.println("SELECT * FROM Customers WHERE Username = \""+user+"\" AND Pass ="+"\""+sha(pass)+"\"");
			boolean exists = rs.next();
			if(exists){
				System.out.println("Logged in");
				LoggedIn=true;
				rs = st.executeQuery("SELECT * FROM accounts WHERE Username = \""+user+"\"");
				while(rs.next()){
					balance = rs.getInt("Balance");
				}
				uname = user;
				System.out.println("current balance : "+balance);
			}
			else{
				System.out.println("Try again");
				login();
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}


}

public class App{
	public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		BankSession user = new BankSession();
		int c;
		//b.connect();
		System.out.println("ENTER CHOICE: \n1.Create Account\n2.Login\n3.Exit\n");
		c=sc.nextInt();
		while(c!=3){
			if(c==1){
				user.createAccount();
			}
			if(c==2){
				user.login();
				if(user.LoggedIn){
					int choice;
					
					System.out.println("Enter Choice :\n 1.Deposit\n2.Withdraw\n3.Transfer\n4.Check Balance\n5.Logout");
					choice = sc.nextInt();
					while(choice!=5){
						if(choice == 1){
							int x;
							System.out.println("Enter the amount to be deposited : ");
							x = sc.nextInt();
							user.balance = user.balance + x;
							Statement st = user.connect();
							try{
								st.executeUpdate("UPDATE accounts SET Balance="+user.balance+" WHERE Username = \""+user.uname+"\"");
								ResultSet rs = st.executeQuery("SELECT * FROM accounts WHERE Username = \""+user+"\"");
								while(rs.next()){
									user.balance = rs.getInt("Balance");
								}
								System.out.println("balance : "+user.balance);
							} catch(Exception e){
								System.out.println(e);
							}
							System.out.println();
						}
						if(choice == 2){
							try{
								int x;
								System.out.println("Enter the amount to withdraw : ");
								x = sc.nextInt();
								if(x>user.balance){
									throw new OutOfBalanceException("Not enough money");
								}
								else{
									user.balance = user.balance-x;
									Statement st = user.connect();
									try{
										st.executeUpdate("UPDATE accounts SET Balance="+user.balance+" WHERE Username = \""+user.uname+"\"");
										ResultSet rs = st.executeQuery("SELECT * FROM accounts WHERE Username = \""+user+"\"");
										while(rs.next()){
											user.balance = rs.getInt("Balance");
										}
										System.out.println("balance : "+user.balance);
										} catch(Exception e){
											System.out.println(e);
										}
								}
							} catch(Exception e){
								System.out.println(e);
							}
						}
						if(choice == 3){
							try{
								int x;
								System.out.println("Enter the amount to transfer : ");
								x = sc.nextInt();
								if(x>user.balance){
									throw new OutOfBalanceException("Can't transfer enough money");
								}
								else{
									String target;
									System.out.println("Enter the user id of person you want to transfer : ");
									target = sc.next();
									
										Statement st = user.connect();
										Statement stmt = user.connect();
										ResultSet rset = st.executeQuery("SELECT * FROM accounts WHERE Username = \""+target+"\"");
										if(!rset.next()){
											System.out.println("User doesn't exists");
										}
										else{
											int u2bal;
											rset = st.executeQuery("SELECT * FROM accounts WHERE Username = \""+target+"\"");
											rset.next();
											u2bal = rset.getInt("Balance");
											user.balance = user.balance - x;
											u2bal = u2bal + x;
											st.executeUpdate("UPDATE accounts SET Balance="+u2bal+" WHERE Username = \""+target+"\"");
											System.out.println("Transferred "+x+" Rupees.");
										}
										
										
									
								}
							} catch(Exception e){
								System.out.println(e);
							}
						}
						if(choice == 4){
							System.out.println("Current balance : "+user.balance);
						}
						System.out.println("Enter Choice :\n 1.Deposit\n2.Withdraw\n3.Transfer\n4.Check Balance\n5.Logout");
						choice = sc.nextInt();
					}
					user.LoggedIn = false;
				}
			}
			if(c==3){
				break;
			}
			System.out.println("ENTER CHOICE: \n1.Create Account\n2.Login\n3.Exit\n");
			c=sc.nextInt();
		}
		
	}
}

class OutOfBalanceException extends Exception{
	OutOfBalanceException(String message){
		super(message);
	}
}
//5 Pages report
//2-3 pages certificate and all
//paste code and problem statement and introduction

//CA-4 and CA-5 same date online quiz java 5th june sec-a