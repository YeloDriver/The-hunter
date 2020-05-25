from django.contrib import admin
from chat.models import Game
from django.contrib.auth.models import Group, User
from channels_presence.models import Room


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
    list_display =('session','user','pos_lat','pos_lng','time',)
    list_filter = (UserFilter, SessionFilter)
    ordering = ('time','user',)
    



admin.site.register(Room)
admin.site.register(Game,GameAdmin)

admin.site.unregister(Group)

admin.site.site_header = 'Admin Page'



# Register your models here.
