// Load required packages
var passport = require('passport');
var BasicStrategy = require('passport-http').BasicStrategy;
var User = require('../models/user');

passport.use(new BasicStrategy(function(email, password, callback) {
  User.findOne({ email: email }, function (err, user) {
    if(err)   return callback(err);

    // No user found with that username
    if(!user) {
      console.log("Username not found");
      return callback(null, false);
    }

    // Make sure the password is correct
    user.verifyPassword(password, function(err, isMatch) {
      if(err) {
        console.log("Auth error 3");
        console.log(err);
        return callback(err);
      }

      // Password did not match
      if(!isMatch) {
        console.log("Incorrect password");
        console.log(err);
        return callback(null, false);
      }

      // Success
      return callback(null, user);
    });
  });
}));

exports.isAuthenticated = passport.authenticate('basic', { session : false });