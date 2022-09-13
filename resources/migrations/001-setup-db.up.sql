create table if not exists auth (
  user_id integer primary key,
  username text not null unique,
  password_hash text not null,
  created_at integer not null default current_timestamp
);


--;;

create table if not exists mazes (
  id integer primary key,
  user_id integer not null,
  configuration text not null,
  min_path text not null,
  max_path text not null,
  created_at integer not null default current_timestamp
);
