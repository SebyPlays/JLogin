# JLogin Documentation

## What is JLogin?
-
```
Simply, JLogin is a fundament of an authentication system, that manages registering users and logging in.
´´´

## How do i use it?
-

The usage of JLogin is very simple, 
you simply just create a new class instance of JLogin and fill the parameters of 
the methods, "login" or "register".

The JLogin constructor requires your SQL details to be used.
  new JLogin(String host, int port, String database, String username, String password);

### Login methods

  This method is ment to be used for local logins that don't require an IP:
  new JLogin("127.0.0.1", 3306, "JAuth", "default", "default").login(<USERNAME>, <PASSWORD>);
  
  The second login method on the other hand, is ment to be used for external logins.
  new JLogin("127.0.0.1", 3306, "JAuth", "default", "default").login(<USERNAME>, <PASSWORD>, <INETADDRESS>);
  
  Both of these methods will return a boolean, so you can wrap them in your own method.
 
 
### register method

  This method creates a user with a very unique uuid, and its given credentials.
  new JLogin("127.0.0.1", 3306, "JAuth", "default", "default").register(<USERNAME>, <PASSWORD>, <HIERARCHY(it's an integer)>);
  
### Options method
  
  This method is an additional method that allows you to configure your auth system precisely.
  new JLogin("127.0.0.1", 3306, "JAuth", "default", "default").
    options(HashType hashType, boolean hash, boolean logHash, boolean limitFalseLogins, int falseLoginLimitation, LoginAffections loginAffections);
    
    The parameter "hashType" is obviously for the hashType you want to configure.
    The parameter "hash" is for configuring if passwords should be hashed.
    The parameter "logHash" is for configuring if a hashLog should be created.
    The parameter "limitFalseLogins" is for configuring, if failed logins should be limited.
    The parameter "falseLoginLimitation" is for configuring the amount of failed logins allowed, before taking affect.
    The parameter "loginAffections" is for configuring the consequence of exceeding the allowed amount of failed logins.

    ex:
      new JLogin("127.0.0.1", 3306, "JAuth", "default", "default").options(HashType.CRC32, true, true, true, 3, LoginAffections.DISABLE_ACCOUNT);

### LoginAffections
  
  The "LoginAffections" class is an enum consisting of 4 elements:
    "DISABLE_ACCOUNT",
    "DELETE_ACCOUNT",
    "SHUTDOWN_AUTHSYSTEM_ALLOW_ADMIN_ONLY",
    "ALLOW_LOCAL_ONLY"

  each of these options mentioned above have a function that
   is occuring when exceeding the failed login limitation.(No shit sherlock).

    The "DISABLE_ACCOUNT" option disables the account of the user, when exceeding the limit.
    The "DELETE_ACCOUNT" option deletes the account of the user immediately, when exceeding the limit.
    The "SHUTDOWN_AUTHSYSTEM_ALLOW_ADMIN_ONLY" option is supposed to shutdown the auth service for external logins than localhost, in addition, the only 
      user that is allowed to log on, is the admin user, when exceeding the limit. (But this one hasn't been added yet)
    The "ALLOW_LOCAL_ONLY" option is only allowing connects from inside the local host...
      WHEN EXCEEDING THE LIMIT :D 
 
### Sessions
  
  This API assigns sessions to users after their login.
  If a user logs on from another location, the user will be suspended from its current session.
  
