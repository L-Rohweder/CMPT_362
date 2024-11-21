import sqlite3 as sql
import configparser
import os

config = configparser.ConfigParser()
config_path = os.path.join(os.path.dirname(__file__), '../config.ini')
config.read(config_path)
db_path = os.path.join(os.path.dirname(__file__),"../database", config.get("Database", "db_name"))
connection = sql.connect(db_path)
cursor = connection.cursor()

# Create users table first (since posts reference it)
cursor.execute("""CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email character varying(256),
    username character varying(256) UNIQUE,
    firstname character varying(256),
    lastname character varying(256),
    gender character varying(256),
    bio character varying(256),
    profile_image character varying(256),
    password character varying(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);""")

# Create posts table with foreign key to users
cursor.execute("""CREATE TABLE IF NOT EXISTS posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name character varying(256),
    content character varying(256),
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    image_link character varying(256),
    username character varying(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);""")

connection.commit()
connection.close()