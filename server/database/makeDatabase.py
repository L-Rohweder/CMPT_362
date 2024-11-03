import sqlite3 as sql
import configparser
import os
config = configparser.ConfigParser()
config_path = os.path.join(os.path.dirname(__file__), '../config.ini')
config.read(config_path)
db_path = os.path.join(os.path.dirname(__file__),"../database", config.get("Database", "db_name"))
connection = sql.connect(db_path)
cursor = connection.cursor()

cursor.execute("""CREATE TABLE IF NOT EXISTS posts (

    name character varying(256) ,
    
    content character varying(256) ,
               
    latitude REAL NOT NULL,
               
    longitude REAL NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);""")

connection.commit()
connection.close()