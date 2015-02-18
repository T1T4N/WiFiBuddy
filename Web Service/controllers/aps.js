var express = require('express');
var router = express.Router();

var mongoose = require('mongoose');
var AccessPoint = require('../models/accesspoint');
var authController = require('../controllers/auth');
var User = require('../models/user');

/**
 * Gets all public Access Points from all other users EXCEPT from the authenticated user.
 */
router.get('/', authController.isAuthenticated, function(req, res) {
  console.log('User: ' + req.user._id + ' trying to GET public APs');
  AccessPoint.find({
    privacyType: 0, 
    publisher: {'$ne': req.user._id}
  })
  .populate('publisher', 'email') // only return the Persons name
  .exec(function(err, aps) {
    if (err)  {
      console.log(err);
      res.send(err);
    } else    res.json(aps);
  });
});

/**
 * Posts a new access point as the authenticated user.
 */
router.post('/', authController.isAuthenticated, function(req, res) {
  console.log('User: ' + req.user._id + ' trying to POST an AP');

  var ap = new AccessPoint();
  ap.bssid = req.body.bssid;
  ap.name = req.body.name;
  ap.password = req.body.password;
  ap.securityType = req.body.securityType;
  ap.privacyType = req.body.privacyType;
  ap.lat = req.body.lat;
  ap.lon = req.body.lon;
  if(req.body.lastAccessed) ap.lastAccessed = req.body.lastAccessed;

  ap.publisher = req.user._id;

  // Save the ap and check for errors
  ap.save(function(err) {
    if (err)  {
      console.log(err);
      res.status(422).json({message: err.message});
    }
    else {
      User.findByIdAndUpdate(ap.publisher, {$push: {accesspoints: ap._id}}, function(err, user) {
        if(err) {
          console.log(err);
          res.send(err);
        }
        else    res.json(ap);
      });
    }
  });
});

router.put('/:ap_id', authController.isAuthenticated, function(req, res) {
  console.log('User: ' + req.user._id + ' trying to PUT AP with id: ' + req.params.ap_id);

  var authenticatedUser = req.user._id;
  AccessPoint.update({_id: req.params.ap_id, publisher: req.user._id}, 
    {
      name: req.body.name, 
      password: req.body.password,
      privacyType: req.body.privacyType,
      lat: req.body.lat,
      lon: req.body.lon,
      lastAccessed: req.body.lastAccessed
    }, function(err, numAffected) {
      if(err) {
        console.log(err);
        res.send(err);
      } else if (numAffected === 0) {
        res.status(401).json({message: "You are not the owner of this AP"});
      } else {
        res.json({message: numAffected});
      }
  });
});

router.delete('/:ap_id', authController.isAuthenticated, function(req, res){
  console.log('User: ' + req.user._id + ' trying to DELETE AP with id: ' + req.params.ap_id);

  AccessPoint.find({_id: req.params.ap_id, publisher: req.user._id}, function(err, aps){
    if (err)  {
      console.log(err);
      res.send(err);
    }
    else if (!aps || !Array.isArray(aps) || aps.length === 0) {
      console.log('No aps found to remove');
      res.status(401).json({message: "You are not the owner of this AP"});
    }
    else {
      aps.forEach( function (ap) {
        ap.remove(function(error) {
          User.findById(req.user._id, function(err, user) {
            user.accesspoints.remove(req.params.ap_id);
            user.save(function(error){
              if (error)  {
                console.log(err);
                res.send(error);
              } else { 
                res.json({ message: 'AP removed' });
              }
            });
          });       
        });
      });
    }
  });
});

module.exports = router;