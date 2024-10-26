import sqlite3 as sql

connection = sql.connect("posts.db")
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