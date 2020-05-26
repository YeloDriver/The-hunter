from django.contrib import admin
from chat.models import Game,ChatRoom,PlayRoom
from django.contrib.auth.models import Group, User
from channels_presence.models import Room
from django.http import HttpResponse

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

class GameAdmin(admin.ModelAdmin):
    def download_csv(self, request, queryset):
        import csv
        from io import StringIO

        f = StringIO()
        writer = csv.writer(f)
        writer.writerow(['session','user','pos_lat','pos_lng','time',])
        for s in queryset:
            writer.writerow([s.session, s.user, s.pos_lat, s.pos_lng, s.time])        
        f.seek(0)
        response = HttpResponse(f, content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename=stat-info.csv'
        return response
    actions = ['download_csv']
    download_csv.short_description = "Download CSV file for selected stats."
    list_display =('session','user','pos_lat','pos_lng','time',)
    list_filter = (UserFilter, SessionFilter)    
    ordering = ('time','user',) 
    
class ChatRoomAdmin(admin.ModelAdmin):
    list_display = ('room_name','room_url',)
    def clean(self,request,queryset):
    	for room in ChatRoom.objects.all():
    	    room.delete()
    	for room in Room.objects.all():
    	    new = ChatRoom(room_name = room.channel_name[5:] ,room_url ="https://127.0.0.1:8000/chat/" + room.channel_name[5:])
    	    new.save()
    actions = ['clean']

class PlayRoomAdmin(admin.ModelAdmin):
    list_display = ('room_name','room_url',)

admin.site.register(Room)
admin.site.register(ChatRoom,ChatRoomAdmin)
admin.site.register(PlayRoom,PlayRoomAdmin)
admin.site.register(Game,GameAdmin)

admin.site.unregister(Group)

admin.site.site_header = 'Admin Page'



# Register your models here.
