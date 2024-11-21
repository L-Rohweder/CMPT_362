import sqlite3 as sql
import configparser
import os
config = configparser.ConfigParser()
config_path = os.path.join(os.path.dirname(__file__), '../config.ini')
config.read(config_path)
db_path = os.path.join(os.path.dirname(__file__),"../database", config.get("Database", "db_name"))
user_db_path = os.path.join(os.path.dirname(__file__),"../database", config.get("Database", "user_db_name"))
connection = sql.connect(db_path)
cursor = connection.cursor()

cursor.execute("""CREATE TABLE IF NOT EXISTS posts (

    name character varying(256) ,
    
    content character varying(256) ,
               
    latitude REAL NOT NULL,
               
    longitude REAL NOT NULL,
               
    image_link character varying(256) ,
               
    user_id INT,
               
    username character varying(256) ,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);""")

connection.commit()
connection.close()

connection = sql.connect(user_db_path)
cursor = connection.cursor()

cursor.execute("""CREATE TABLE IF NOT EXISTS users (

    id INT ,
    
    email character varying(256) ,
               
    username character varying(256),
               
    firstname character varying(256),
               
    lastname character varying(256) ,
               
    gender character varying(256) ,
               
    bio character varying(256) ,
               
    profile_image character varying(256),
               
    password character varying(256),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);""")

connection.commit()
connection.close()