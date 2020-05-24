from django.urls import re_path
from django.conf.urls import url
from channels.routing import ProtocolTypeRouter, URLRouter
from channels.auth import AuthMiddlewareStack

from . import consumers

websocket_urlpatterns = [
        re_path(r'wss/chat/(?P<room_name>\w+)/$', consumers.ChatConsumer),
        re_path(r'wss/chat/(?P<room_name>\w+)/play/$', consumers.PlayConsumer)
]

application = ProtocolTypeRouter({

    "websocket": AuthMiddlewareStack(
        URLRouter([
            url(r"^front(end)/$", consumers.ChatConsumer),
        ])
    ),

})
