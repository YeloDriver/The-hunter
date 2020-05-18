from django.shortcuts import render

from channels_presence.models import Room 

def rooms(request):
    rooms_name=""
    for rooms in Room.objects.all():
            print(rooms.channel_name[5:])
            rooms_name += rooms.channel_name[5:] + " ; "
    return render(request, 'rooms.html' , {
        'rooms_name' : rooms_name
    })