create table if not exists auth (
  user_id integer primary key,
  username text not null unique,
  password_hash text not null,
  created_at integer not null default current_timestamp
);
