package in.reqres.models;

public class CreateUserRequest{
	private String password = "pistol";
	private String email = "eve.holt@reqres.in";

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return password;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

}


