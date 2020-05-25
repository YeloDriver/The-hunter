from django.contrib import admin
from chat.models import Game
from django.contrib.auth.models import Group, User
from channels_presence.models import Room

class GameAdmin(admin.ModelAdmin):
    list_display=('session','user','pos_lat','pos_lng','time',)
    list_filter= ('session',)

admin.site.register(Room)
admin.site.register(Game,GameAdmin)

admin.site.unregister(Group)

admin.site.site_header = 'Admin Page'



# Register your models here.
