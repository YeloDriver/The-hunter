import json
from channels.generic.websocket import WebsocketConsumer
from asgiref.sync import async_to_sync
from django.contrib.auth.models import Group

class ChatConsumer(WebsocketConsumer):
    def connect(self):
        #print("DEBUG : connection")
        self.room_name = self.scope['url_route']['kwargs']['room_name']
        self.room_group_name = 'chat_%s' % self.room_name
        self.user = self.scope["user"]
        grp_room = Group.objects.get(name='room_'+self.room_name)

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

        self.accept()

    def disconnect(self, close_code):
        # Leave room group
        async_to_sync(self.channel_layer.group_discard)(
            self.room_group_name,
            self.channel_name
        )

    # Receive message from WebSocket
    def receive(self, text_data):
        #print("DEBUG : msg receive")

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
                }
            )
        #print("DEBUG : msg transmit au groupe")


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
        #print("DEBUG : Message Hunter recu du groupe")

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        self.user.groups.add(grp_hunter)
        if self.user.groups.filter(name="hunted_"+self.room_name):
            self.user.groups.remove(grp_hunted)

        hunted=[]
        for u in grp_hunted.user_set.all():
            hunted.append(u.username)
        #print("hunted : "+ str(hunted))

        hunter=[]
        for u in grp_hunter.user_set.all():
            hunter.append(u.username)
        #hunter.append("test")
        #print("hunter : "+ str(hunter))


        msg_type = 'role.update' 
        self.send(text_data=json.dumps({
            'type': msg_type,
            'hunter_list':hunter,
            'hunted_list':hunted
        }))
        
    def hunted_message(self, event):
        #print("DEBUG : Message Hunted recu du groupe")

        grp_hunter = Group.objects.get(name='hunter_'+self.room_name)
        grp_hunted = Group.objects.get(name='hunted_'+self.room_name)
        self.user.groups.add(grp_hunted)
        if self.user.groups.filter(name="hunter_"+self.room_name):
            self.user.groups.remove(grp_hunter)
        
        hunted=[]
        for u in grp_hunted.user_set.all():
            hunted.append(u.username)
        #print("hunted : "+ str(hunted))

        hunter=[]
        for u in grp_hunter.user_set.all():
            hunter.append(u.username)
        #print("hunter : "+ str(hunter))


        msg_type = 'role.update'
        self.send(text_data=json.dumps({
            'type': msg_type,
            'hunter_list':hunter,
            'hunted_list': hunted
        }))

