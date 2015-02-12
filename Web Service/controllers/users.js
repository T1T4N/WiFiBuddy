var express = require('express');
var router = express.Router();

var mongoose = require('mongoose');
var User = require('../models/user');
var AccessPoint = require('../models/accesspoint');
var authController = require('../controllers/auth');

/* BATCH FUNCTIONS */
router.get('/', authController.isAuthenticated, function(req, res) {
  User.find(function(err, users) {
    if (err)  res.send(err);
    else      res.json(users);
  });
});

router.post('/', function(req, res) {
  User.create(req.body, function (err, user) {
    if (err)  res.status(422).send(err);
    else      res.json(user);
  });
});

// TODO: Finish & test this code
router.get('/Authenticated', authController.isAuthenticated, function(req, res){
  User.findById(req.user._id, function(err, user){
    if(err){
      console.log("Mongoose error");
      console.log(err);
      res.send(err);
    }
    else if (!user){
      res.status(404).json({message: "No user with the specified user id exists"});
    }
    else{
      res.json(user);
    }
  });
});
router.put('/Authenticated', authController.isAuthenticated, function(req, res){
  User.findById(req.user._id, function(err, user) {
    if (err)  res.send(err);
    else if (!user){
      res.status(404).json({message: "No user with the specified user id exists"});
    }
    else {
      if(req.body.email)  user.email = req.body.email;
      user.password = req.body.password;

      user.save(function(err2) {
        if (err2) res.status(422).json({message: err2});
        else      res.json(user);
      });
    }
  });
});
router.get('/Authenticated/AccessPoints', authController.isAuthenticated, function(req, res) {
  AccessPoint.find({publisher: req.user._id}, function(err, aps) {
    if (err)  res.send(err);
    else if (!aps){
      res.status(404).json({message: "No APs for the specified user id exist"});
    }
    else {
      res.json(aps);
    }
  });
});

/* PER ELEMENT FUNCTIONS */
router.get('/:user_id', authController.isAuthenticated, function(req, res) {
  User.findById(req.params.user_id, function(err, user) {
    if (err) {      
      console.log("Mongoose error");
      console.log(err);
      res.send(err);
    }
    else if (!user) {
      res.status(404).json({message: "No user with the specified user id exists"});
    }
    else {
      res.json(user);
    }
  });
});

/**
 * DEBUG purposes
 */
router.get('/:user_id/AccessPoints', authController.isAuthenticated, function(req, res) {
  AccessPoint.find({publisher: req.params.user_id}, function(err, aps) {
    if (err)  res.send(err);
    else      res.json(aps);
  });
});

/**
 * DEBUG purposes
 */
router.delete('/:user_id/:ap_id', authController.isAuthenticated, function(req, res){
  User.findById(req.params.user_id, function(err, user) {
    
    user.accesspoints.remove(req.params.ap_id);
    user.save(function(error){
      if (error) res.send(error);
      else       res.json({ message: 'AP removed' });
    });
  });
});     

// TODO: Remove method
router.delete('/:user_id', authController.isAuthenticated, function(req, res){
  User.findByIdAndRemove(req.params.user_id, function(err) {
    if (err)  res.send(err);
    else      res.json({ message: 'User removed' });
  });
});

module.exports = router;