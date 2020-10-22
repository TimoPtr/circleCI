# What is it?
This module exposes anonymized interfaces to the SDK

## Usage
The main class to look at is AccountFacade 

It is very important that clients subscribe to `AccountFacade.shouldLogout` in all their activities
and react by placing the user in the login screen. Using the SDK without login in again will
result in unexpected behaviour  
