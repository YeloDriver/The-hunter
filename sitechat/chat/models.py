from django.db import models

class Game(models.Model):
    session = models.CharField(max_length = 30) # nom de chatroom
    user = models.CharField(max_length = 30)
    pos_lng = models.FloatField()
    pos_lat = models.FloatField()
    time = models.FloatField()
    team = models.CharField(max_length = 30)

class ChatRoom(models.Model):
    room_name = models.CharField(max_length = 30)
    room_url = models.CharField(max_length = 50)

class PlayRoom(models.Model):
    room_name = models.CharField(max_length = 30)
    room_url = models.CharField(max_length = 50)
