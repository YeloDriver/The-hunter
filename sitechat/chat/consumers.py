import json
import time

from channels.generic.websocket import WebsocketConsumer
from asgiref.sync import async_to_sync
from django.contrib.auth.models import Group, User

from channels_presence.models import Room , Presence

from channels.layers import get_channel_layer
from channels_presence.signals import presence_changed

from chat.models import Game,ChatRoom,PlayRoom


channel_layer = get_channel_layer()

class ChatConsumer(WebsocketConsumer):
    def connect(self):
        #print("DEBUG : connection")
        self.room_name = self.scope['url_route']['kwargs']['room_name']
        self.room_group_name = 'chat_%s' % self.room_name
        self.user = self.scope["user"]
        grp_room = Group.objects.get(name='room_'+self.room_name)
        create = True
        for r in ChatRoom.objects.all():
            if (r.room_name == self.room_group_name[5:]) and (r.room_url =="https://127.0.0.1/chat/"+self.room_group_name[5:] ):
                create = False
        if create == True:
            room_model = ChatRoom(room_name = self.room_group_name[5:],room_url ="https://127.0.0.1/chat/"+self.room_group_name[5:] )
            room_model.save()

        self.user.groups.add(grp_room)
         
        users = []
        for u in grp_room.user_set.all():
            users.append(u.username)
        #print(users)

        # Join room group
        async_to_sync(self.channel_layer.group_add)(
            self.room_group_name,
            self.channel_name
        )
        self.room = Room.objects.add(self.room_group_name, self.channel_name, self.user)
        print("Status update")
        num = 0
        for user in self.room.get_users():
            num += 1
            print(user.username + " is connected to chat room " + self.room_group_name)
        print(str(num) + " players in chat room" + self.room_group_name)

        self.accept()


    def disconnect(self, close_code):
        # Leave room group
        async_to_sync(self.channel_layer.group_discard)(
            self.room_group_name,
            self.channel_name
        )
        Room.objects.remove(self.room_group_name, self.channel_name)
        print("Status update")
        num = 0
        for user in self.room.get_users():
            num += 1
            print(user.username + " is connected to chat room " + self.room_group_name)
        print(str(num) + " players in chat room" + self.room_group_name)

    # Receive message from WebSocket
    def receive(self, text_data):
        #print("DEBUG : msg receive")
        Presence.objects.touch(self.channel_name)

        text_data_json = json.loads(text_data)
        msg_type = text_data_json.get('type')
        msg_user = self.scope["user"].username
        #msg_user = text_data_json.get('user')
        if msg_type == "chat.message":
            text = text_data_json.get("text")
            # Send message to room group
            async_to_sync(self.channel_layer.group_send)(
                self.room_group_name,
                {   
                    'type': msg_type,
                    'user': msg_user,
                    'text': text
                }
            )

        else:
            async_to_sync(self.channel_layer.group_send)(
                self.room_group_name,
                {
                    'type': msg_type,
                    'user': msg_user
                }
            )
        if msg_type == "heart_beat":
            print("dcmm")
            Presence.objects.touch(self.channel_name)
        #print("DEBUG : msg transmit au groupe")
        
    def heart_beat(self, event):
        msg_type = event['type']
        msg_user = event['user']
        Presence.objects.touch(self.channel_name)

    def chat_message(self, event):
        #print("DEBUG : chat Message recu du groupe")
        msg_type = event['type']
        msg_user = event['user']
        text = event['text']
        # Send message to WebSocket
        self.send(text_data=json.dumps({
            'type': msg_type,
            'user': msg_user,
            'text': text
        }))

    def hunter_message(self, event):

        msg_user = event['user']
        userObject = User.objects.get(username=msg_user)

        #print("DEBUG : Message Hunter recu du groupe")

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        userObject.groups.add(grp_hunter)
        if userObject.groups.filter(name="hunted_"+self.room_name):
            userObject.groups.remove(grp_hunted)
        
        #print("DEBUG : role update : "+userObject.username+" -> Hunter")
        hunted=[]
        for u in grp_hunted.user_set.all():
            hunted.append(u.username)
        #print("hunted : "+ str(hunted))

        hunter=[]
        for u in grp_hunter.user_set.all():
            hunter.append(u.username)
        #print("hunter : "+ str(hunter))
        #print("\n")

        msg_type = 'role.update' 
        self.send(text_data=json.dumps({
            'type': msg_type,
            'hunter_list':hunter,
            'hunted_list':hunted
        }))
        
    def hunted_message(self, event):
        
        #print("DEBUG : Message Hunted recu du groupe")

        msg_user = event['user']
        userObject = User.objects.get(username=msg_user)

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        userObject.groups.add(grp_hunted)
        #print("added"+userObject.username+" to grp_hunted")
        if userObject.groups.filter(name="hunter_"+self.room_name):
            userObject.groups.remove(grp_hunter)
            #print("deleted"+userObject.username+"from grp_hunter")

        #print("DEBUG : role update : "+userObject.username+" -> Hunted")
        
        hunted=[]
        for u in grp_hunted.user_set.all():
            hunted.append(u.username)
        #print("hunted : "+ str(hunted))

        hunter=[]
        for u in grp_hunter.user_set.all():
            hunter.append(u.username)
        #print("hunter : "+ str(hunter))
        #print("\n")

        msg_type = 'role.update'
        self.send(text_data=json.dumps({
            'type': msg_type,
            'hunter_list':hunter,
            'hunted_list': hunted
        }))
    
    def start_message(self, event):
        msg_type = event['type']
        self.send(text_data=json.dumps({
            'type': msg_type,
        }))

    def clean_message(self, event):
        msg_type = event['type']
        userObject = self.scope['user']

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        if userObject.groups.filter(name="hunter_"+self.room_name):
            userObject.groups.remove(grp_hunter)
        elif userObject.groups.filter(name="hunted_"+self.room_name):
            userObject.groups.remove(grp_hunted)
        self.send(text_data=json.dumps({
            'type': msg_type,
        }))

class PlayConsumer(WebsocketConsumer):
    def connect(self):
        print("DEBUG : connection")
        self.room_name = self.scope['url_route']['kwargs']['room_name']
        self.room_group_name = 'chat_%s' % self.room_name
        self.user = self.scope["user"]
        
        create = True
        for r in PlayRoom.objects.all():
            if (r.room_name == self.room_group_name[5:]) and (r.room_url == "https://127.0.0.1/chat/" + self.room_group_name[5:] +"/play"):
                create = False
        if create == True:
            room_model = PlayRoom(room_name = self.room_group_name[5:],room_url ="https://127.0.0.1/chat/"+self.room_group_name[5:] +'/play')
            room_model.save()

         
        # Join room group
        async_to_sync(self.channel_layer.group_add)(
            self.room_group_name,
            self.channel_name
        )

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)    #create a local copy of hunter and hunted
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        self.hunted=[]
        for u in grp_hunted.user_set.all():
            self.hunted.append(u.username)
        self.hunter=[]
        for u in grp_hunter.user_set.all():
            self.hunter.append(u.username)
        
        self.timestart = time.time()
        self.gamelength = 1800 #réglable pour ajuster longueur partie, en s (1800=30min)

        self.room = Room.objects.add("play"+self.room_group_name[4:], self.channel_name, self.user)        #list all the users in the room and print them and send them to the group
        print("Status update")
        num = 0
        userlist = []
        for user in self.room.get_users():
            num += 1
            userlist.append(user.username)
            print(user.username + " is connected to chat room " + self.room_group_name)
        print(str(num) + " players in chat room" + self.room_group_name)

        self.accept()

    def disconnect(self, close_code):
        # Leave room group
        Room.objects.remove("play"+self.room_group_name[4:], self.channel_name)
        async_to_sync(self.channel_layer.group_discard)(
            self.room_group_name,
            self.channel_name
        )

    # Receive message from WebSocket
    def receive(self, text_data):
        Presence.objects.touch(self.channel_name)
        
        text_data_json = json.loads(text_data)
        msg_type = text_data_json.get('type')
        msg_user = self.scope["user"].username
        
        print("DEBUG : msg receive "+msg_type)

        if time.time() - self.timestart > self.gamelength:
            msg_type = 'timeout.message'

        if msg_type == 'position.message':
            msg_lat = text_data_json.get('lat')
            msg_lng = text_data_json.get('lng')
            '''if msg_user in self.hunter:
                game_data = Game(session = self.room_name, user = msg_user, pos_lat = msg_lat, pos_lng = msg_lng, time = time.time(),team = "hunter")
                game_data.save()
            else:
                game_data = Game(session = self.room_name, user = msg_user, pos_lat = msg_lat, pos_lng = msg_lng, time = time.time(),team = "hunted")
                game_data.save()'''
		    
            async_to_sync(self.channel_layer.group_send)(
                self.room_group_name,
                {   
                    'type': msg_type,
                    'user': msg_user,
                    'lat': msg_lat,
                    'lng': msg_lng
                }
            )
        elif msg_type == 'liste.message' :
            self.room = Room.objects.add(self.room_group_name, self.channel_name, self.user)
            users = []
            role = []
            for user in self.room.get_users():
                if user.username in self.hunter:
                    role.append("hunter")
                else:
                    role.append("hunted")
                users.append(user.username)
            
            async_to_sync(self.channel_layer.group_send)(
                    self.room_group_name,
                    {
                        'type': 'liste_message',
                        'user': users,
                        'role': role
                    }   
            ) 
        elif msg_type == 'timeout.message': 
            async_to_sync(self.channel_layer.group_send)(
                    self.room_group_name,
                    {
                        'type': msg_type,
                    }   
            )  

    def position_message(self, event):
        msg_type = event['type']
        msg_user = event['user']
        msg_lat = event['lat']
        msg_lng = event['lng']
        self.send(text_data=json.dumps({
            'type': msg_type,
            'user': msg_user,
            'lat': msg_lat,
            'lng': msg_lng,
        }))
        #print("DEBUG : position envoyé au groupe")

    def timeout_message(self,event):
        msg_type = event['type']
        self.send(text_data=json.dumps({
            'type': msg_type,
        }))
        #print("DEBUG : timeout envoyé au groupe")


    def liste_message(self, event):
        msg_type = event['type']
        msg_user = event['user']
        msg_role = event['role']
        self.send(text_data=json.dumps({
            'type': msg_type,
            'user': msg_user,
            'role': msg_role
        }))
        #print("DEBUG : initialisation markers")
