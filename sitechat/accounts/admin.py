from django.contrib import admin
from chat.models import Game,ChatRoom,PlayRoom,End
from django.contrib.auth.models import Group, User
from channels_presence.models import Room
from django.http import HttpResponse
import csv
from io import StringIO

# Config
admin.site.site_header = 'Admin Page'
admin.site.unregister(Group)

# Game filters
class InputFilter(admin.SimpleListFilter): 
    template = 'admin/input_filter.html'
    def lookups(self, request, model_admin):
        # Dummy, required to show the filter.
        return ((),)

    def choices(self, changelist):
        # Grab only the "all" option.
        all_choice = next(super().choices(changelist))
        all_choice['query_parts'] = (
            (k, v)
            for k, v in changelist.get_filters_params().items()
            if k != self.parameter_name
        )
        yield all_choice

class UserFilter(InputFilter):
    title = 'User'
    parameter_name = 'user'

    def queryset(self, request, queryset):
        if self.value():
            return queryset.filter(user=self.value())

class SessionFilter(InputFilter):
    title = 'Session'
    parameter_name = 'session'

    def queryset(self, request, queryset):
        if self.value():
            return queryset.filter(session=self.value())

# Models

class GameAdmin(admin.ModelAdmin):
    # Functions
    def download_csv(self, request, queryset):        
        f = StringIO()
        writer = csv.writer(f)
        writer.writerow(['session','user','pos_lat','pos_lng','time',])
        for s in queryset:
            writer.writerow([s.session, s.user, s.pos_lat, s.pos_lng, s.time])        
        f.seek(0)
        response = HttpResponse(f, content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename=stat-info.csv'
        return response

    # Elements
    list_display =('session','user','pos_lat','pos_lng','time',)
    list_filter = (UserFilter, SessionFilter)    
    ordering = ('time','user',) 
    actions = ['download_csv']
    download_csv.short_description = "Download CSV file for selected stats."
      
class ChatRoomAdmin(admin.ModelAdmin):
    # Functions
    def download_csv(self, request, queryset):        
        f = StringIO()
        writer = csv.writer(f)
        writer.writerow(['room_name','room_url',])
        for s in queryset:
            writer.writerow([s.room_name, s.room_url])        
        f.seek(0)
        response = HttpResponse(f, content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename=stat-info.csv'
        return response
    
    def clean(self,request,queryset):
    	for room in ChatRoom.objects.all():
    	    room.delete()
    	for room in Room.objects.all():
    	    new = ChatRoom(room_name = room.channel_name[5:] ,room_url ="https://127.0.0.1:8000/chat/" + room.channel_name[5:])
    	    new.save()

    # Elements
    list_display = ('room_name','room_url',)
    actions = ['clean','download_csv']    
    clean.short_description = "Clean Room"
    download_csv.short_description = "Download CSV file for selected stats."
    
class PlayRoomAdmin(admin.ModelAdmin):
    # Functions
    def download_csv(self, request, queryset):        
        f = StringIO()
        writer = csv.writer(f)
        writer.writerow(['room_name','room_url',])
        for s in queryset:
            writer.writerow([s.room_name, s.room_url])            
        f.seek(0)
        response = HttpResponse(f, content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename=stat-info.csv'
        return response
    
    # Elements
    list_display = ('room_name','room_url',)

    actions = ['download_csv']
    download_csv.short_description = "Download CSV file for selected stats."

class EndAdmin(admin.ModelAdmin):
    # Functions
    def download_csv(self, request, queryset):        
        f = StringIO()
        writer = csv.writer(f)
        writer.writerow(['winner','session','pos_lat','pos_lng','time',])
        for s in queryset:
            writer.writerow([s.winner, s.session])        
        f.seek(0)
        response = HttpResponse(f, content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename=stat-info.csv'
        return response
    
    # Elements
    list_display = ('winner','session',)
    actions = ['download_csv']
    download_csv.short_description = "Download CSV file for selected stats."
    
admin.site.register(Room)
admin.site.register(End,EndAdmin)
admin.site.register(ChatRoom,ChatRoomAdmin)
admin.site.register(PlayRoom,PlayRoomAdmin)
admin.site.register(Game,GameAdmin)








