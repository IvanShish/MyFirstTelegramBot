DROP TABLE IF EXISTS public.reminders;
DROP TABLE IF EXISTS public.users;

CREATE TABLE public.users
(
    id         SERIAL  PRIMARY KEY,
    chat_id    INTEGER UNIQUE                NOT NULL,
    name       VARCHAR                       NOT NULL,
    location   VARCHAR,
    mode       INTEGER
);

CREATE TABLE public.reminders
(
    id         SERIAL  PRIMARY KEY,
    user_id    INTEGER REFERENCES users (id) ON DELETE cascade,
    reminder   VARCHAR                       NOT NULL
);

SELECT * FROM public.users;
SELECT * FROM public.reminders;
SELECT * FROM public.quiz;

SELECT question FROM public.quiz
ORDER BY id ASC
LIMIT 1 WHERE user_id=1;

ALTER TABLE public.users ADD state VARCHAR;
ALTER TABLE public.users ADD best_score INTEGER;

CREATE TABLE public.quiz
(
    id         SERIAL  PRIMARY KEY,
    user_id    INTEGER REFERENCES users (id) ON DELETE cascade,
    question   VARCHAR                       NOT NULL,
    answer   VARCHAR                       NOT NULL
);
