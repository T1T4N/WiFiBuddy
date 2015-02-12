// Get the packages we need
var express = require('express');
var mongoose = require('mongoose');
var bodyParser = require('body-parser');
var passport = require('passport');
var https = require('https');
var http = require('http');
var fs = require('fs');

mongoose.connect('mongodb://localhost:27017/wifibuddy');

// Setting up express
var app = express();
app.use(bodyParser.urlencoded({
  extended: true
}));
app.use(bodyParser.json());
app.use(passport.initialize());

var port = process.env.PORT || 3000;

// Initial dummy route for testing
var router = express.Router();
router.get('/', function(req, res) {
  res.json({ message: 'You are running dangerously low on beer!' });
});

// Setting up routes
var usersRouter = require('./controllers/users');
var apsRouter = require('./controllers/aps');
app.use('/Users', usersRouter);
app.use('/AccessPoints', apsRouter);

// Starting up HTTPS server
var sslKey = fs.readFileSync('./ssl/server.key');
var sslCert = fs.readFileSync('./ssl/server.crt');
//https.createServer({key: sslKey,cert: sslCert}, app).listen(port);
http.createServer(app).listen(port);
console.log('Insert beer on port ' + port);