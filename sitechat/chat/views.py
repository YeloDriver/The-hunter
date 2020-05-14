from django.shortcuts import render
from django.contrib.auth.models import Group, Permission

def index(request):
    return render(request, 'chat/index.html')

def room(request, room_name):
    grp_room = Group.objects.get_or_create(name="room_"+room_name)
    grp_hunter = Group.objects.get_or_create(name="hunter_"+room_name)
    grp_hunted = Group.objects.get_or_create(name="hunted_"+room_name)
    return render(request, 'chat/room.html', {
        'room_name': room_name
    })

def play(request, room_name):
    return render(request, 'chat/play.html', {
        'room_name': room_name
        })
# Create your views here.


