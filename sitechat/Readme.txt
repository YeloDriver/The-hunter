pip install -r requirements.txt (in a virtualenv preferably)
(sudo) docker run -p 6379:6379 -d redis:5

python3 manage.py runserver

go to http://127.0.0.1:8000 

Si la base de données ne marche pas :
python manage.py makemigrations
python manage.py migrate --run-syncdb
