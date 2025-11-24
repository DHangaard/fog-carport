
BEGIN;

CREATE TABLE IF NOT EXISTS public.zip_code (
zip_code integer NOT NULL,
city character varying NOT NULL,
CONSTRAINT zip_code_pkey PRIMARY KEY (zip_code)
    );

CREATE TABLE IF NOT EXISTS public.users (
user_id serial NOT NULL,
first_name character varying(50) NOT NULL,
last_name character varying(50) NOT NULL,
email character varying(100) NOT NULL,
phone_number character varying(20) NOT NULL,
hashed_password character varying(150) NOT NULL,
zip_code integer NOT NULL,
street character varying(100) NOT NULL,
role character varying NOT NULL DEFAULT 'CUSTOMER',
CONSTRAINT users_pkey PRIMARY KEY (user_id),
CONSTRAINT users_email_key UNIQUE (email),
CONSTRAINT users_zip_code_fk FOREIGN KEY (zip_code)
REFERENCES public.zip_code (zip_code)
ON UPDATE CASCADE
ON DELETE RESTRICT
);


CREATE TABLE IF NOT EXISTS public.shed (
shed_id serial NOT NULL,
length integer NOT NULL,
width integer NOT NULL,
shed_placement character varying, NOT NULL,

     CONSTRAINT shed_pkey PRIMARY KEY (shed_id)
      );


CREATE TABLE IF NOT EXISTS public.carport (
carport_id serial NOT NULL,
length integer NOT NULL,
width integer NOT NULL,
shed_id integer,
roof_type character varying(30) NOT NULL DEFAULT 'FLAT',

    CONSTRAINT carport_pkey PRIMARY KEY (carport_id),
    CONSTRAINT carport_shed_id_unique UNIQUE (shed_id),
    CONSTRAINT carport_shed_fk FOREIGN KEY (shed_id)
    REFERENCES public.shed (shed_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
    );


CREATE TABLE IF NOT EXISTS public.material (
material_id serial NOT NULL,
name character varying(100) NOT NULL,
category character varying NOT NULL,
type character varying NOT NULL,
material_width integer,
material_height integer,
unit character varying(20) NOT NULL,
usage character varying,

CONSTRAINT material_pkey PRIMARY KEY (material_id)
    );

CREATE TABLE IF NOT EXISTS public.material_variant (
material_variant_id serial NOT NULL,
material_id integer NOT NULL,
variant_length integer,
unit_price double precision NOT NULL,

CONSTRAINT material_variant_pkey PRIMARY KEY (material_variant_id),
CONSTRAINT material_variant_material_fk FOREIGN KEY (material_id)
REFERENCES public.material (material_id)
ON UPDATE CASCADE
ON DELETE CASCADE);


CREATE TABLE IF NOT EXISTS public."order" (
order_id serial NOT NULL,
customer_id integer NOT NULL,
seller_id integer,
carport_id integer NOT NULL,
request_created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_at timestamp without time zone,
expiration_date timestamp without time zone,
order_status character varying NOT NULL DEFAULT 'PENDING',
customer_comment text,
coverage_percentage double precision,
cost_price double precision

    CONSTRAINT order_pkey PRIMARY KEY (order_id),

    CONSTRAINT order_customer_fk FOREIGN KEY (customer_id)
    REFERENCES public.users (user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

    CONSTRAINT order_seller_fk FOREIGN KEY (seller_id)
    REFERENCES public.users (user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

    CONSTRAINT order_carport_fk FOREIGN KEY (carport_id)
    REFERENCES public.carport (carport_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
    );


CREATE TABLE IF NOT EXISTS public.material_line (
material_line_id serial NOT NULL,
order_id integer NOT NULL,
material_variant_id integer NOT NULL,
quantity integer NOT NULL,

    CONSTRAINT material_line_pkey PRIMARY KEY (material_line_id),

    CONSTRAINT material_line_order_fk FOREIGN KEY (order_id)
    REFERENCES public."order" (order_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

    CONSTRAINT material_line_variant_fk FOREIGN KEY (material_variant_id)
    REFERENCES public.material_variant (material_variant_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
    );

CREATE SCHEMA IF NOT EXISTS test;

COMMIT;
