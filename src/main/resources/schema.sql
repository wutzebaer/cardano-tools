--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 11.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.account (
    key character varying(255) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    last_update timestamp without time zone NOT NULL,
    policy text,
    policy_due_date timestamp without time zone NOT NULL,
    policy_id character varying(255),
    stake bigint DEFAULT 0 NOT NULL,
    address_id bigint NOT NULL
);


ALTER TABLE public.account OWNER TO peter;

--
-- Name: account_funding_addresses; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.account_funding_addresses (
    account_key character varying(255) NOT NULL,
    funding_addresses character varying(255)
);


ALTER TABLE public.account_funding_addresses OWNER TO peter;

--
-- Name: account_funding_addresses_history; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.account_funding_addresses_history (
    account_key character varying(255) NOT NULL,
    funding_addresses_history character varying(255)
);


ALTER TABLE public.account_funding_addresses_history OWNER TO peter;

--
-- Name: address; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.address (
    id bigint NOT NULL,
    address character varying(255),
    balance bigint NOT NULL,
    skey character varying(255),
    tokens_data text,
    vkey character varying(255)
);


ALTER TABLE public.address OWNER TO peter;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: peter
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO peter;

--
-- Name: mint_order_submission; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.mint_order_submission (
    id bigint NOT NULL,
    target_address character varying(255),
    tip boolean NOT NULL
);


ALTER TABLE public.mint_order_submission OWNER TO peter;

--
-- Name: mint_order_submission_tokens; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.mint_order_submission_tokens (
    mint_order_submission_id bigint NOT NULL,
    tokens_id bigint NOT NULL
);


ALTER TABLE public.mint_order_submission_tokens OWNER TO peter;

--
-- Name: registration_metadata; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.registration_metadata (
    id bigint NOT NULL,
    asset_name character varying(255),
    description character varying(255),
    logo bytea,
    name character varying(255),
    policy character varying(255),
    policy_id character varying(255),
    policy_skey character varying(255),
    ticker character varying(255),
    url character varying(255)
);


ALTER TABLE public.registration_metadata OWNER TO peter;

--
-- Name: token_offer; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.token_offer (
    id bigint NOT NULL,
    asset_name character varying(255),
    canceled boolean NOT NULL,
    created_at timestamp without time zone NOT NULL,
    policy_id character varying(255),
    price bigint NOT NULL,
    token_data text,
    account_key character varying(255) NOT NULL,
    address_id bigint NOT NULL,
    transaction_id bigint,
    error text
);


ALTER TABLE public.token_offer OWNER TO peter;

--
-- Name: token_submission; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.token_submission (
    id bigint NOT NULL,
    amount bigint NOT NULL,
    asset_name character varying(255),
    meta_data text DEFAULT '{}'::text
);


ALTER TABLE public.token_submission OWNER TO peter;

--
-- Name: transaction; Type: TABLE; Schema: public; Owner: peter
--

CREATE TABLE public.transaction (
    id bigint NOT NULL,
    fee bigint NOT NULL,
    inputs text,
    meta_data_json text,
    min_output bigint,
    outputs text,
    policy text,
    policy_id character varying(255),
    raw_data text,
    signed_data text,
    tx_id character varying(255),
    tx_size bigint DEFAULT 0 NOT NULL,
    account_key character varying(255) NOT NULL,
    mint_order_submission_id bigint
);


ALTER TABLE public.transaction OWNER TO peter;

--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (key);


--
-- Name: address address_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.address
    ADD CONSTRAINT address_pkey PRIMARY KEY (id);


--
-- Name: mint_order_submission mint_order_submission_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.mint_order_submission
    ADD CONSTRAINT mint_order_submission_pkey PRIMARY KEY (id);


--
-- Name: registration_metadata registration_metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.registration_metadata
    ADD CONSTRAINT registration_metadata_pkey PRIMARY KEY (id);


--
-- Name: token_offer token_offer_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_offer
    ADD CONSTRAINT token_offer_pkey PRIMARY KEY (id);


--
-- Name: token_submission token_submission_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_submission
    ADD CONSTRAINT token_submission_pkey PRIMARY KEY (id);


--
-- Name: transaction transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT transaction_pkey PRIMARY KEY (id);


--
-- Name: mint_order_submission_tokens uk_3p0pfc9asrb0p2cfby9sgvcku; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.mint_order_submission_tokens
    ADD CONSTRAINT uk_3p0pfc9asrb0p2cfby9sgvcku UNIQUE (tokens_id);


--
-- Name: token_offer ukcj0hvlhbjytxyvfx2shi2i7nm; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_offer
    ADD CONSTRAINT ukcj0hvlhbjytxyvfx2shi2i7nm UNIQUE (policy_id, asset_name, account_key, transaction_id);


--
-- Name: registration_metadata ukge0wnd8i278ft3cokijpgtq9w; Type: CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.registration_metadata
    ADD CONSTRAINT ukge0wnd8i278ft3cokijpgtq9w UNIQUE (policy_id, asset_name);


--
-- Name: token_offer fk3iowyx1gd5ja74af835s1o8xa; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_offer
    ADD CONSTRAINT fk3iowyx1gd5ja74af835s1o8xa FOREIGN KEY (account_key) REFERENCES public.account(key);


--
-- Name: account fk9lna4d7ow9qbs27m5psafys58; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT fk9lna4d7ow9qbs27m5psafys58 FOREIGN KEY (address_id) REFERENCES public.address(id);


--
-- Name: token_offer fkdxjinxyagscjjavxcmay9xl03; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_offer
    ADD CONSTRAINT fkdxjinxyagscjjavxcmay9xl03 FOREIGN KEY (transaction_id) REFERENCES public.transaction(id);


--
-- Name: token_offer fkfpv96e4o1dcwi1voulp3ro8a5; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.token_offer
    ADD CONSTRAINT fkfpv96e4o1dcwi1voulp3ro8a5 FOREIGN KEY (address_id) REFERENCES public.address(id);


--
-- Name: account_funding_addresses fkmxq9nf0ipme16lijyroc4re58; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.account_funding_addresses
    ADD CONSTRAINT fkmxq9nf0ipme16lijyroc4re58 FOREIGN KEY (account_key) REFERENCES public.account(key);


--
-- Name: transaction fkn9x1r5srvkm1nehweldfnn4nf; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT fkn9x1r5srvkm1nehweldfnn4nf FOREIGN KEY (mint_order_submission_id) REFERENCES public.mint_order_submission(id);


--
-- Name: mint_order_submission_tokens fkone16rk5nfyxl6ag5l5goo6e5; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.mint_order_submission_tokens
    ADD CONSTRAINT fkone16rk5nfyxl6ag5l5goo6e5 FOREIGN KEY (tokens_id) REFERENCES public.token_submission(id);


--
-- Name: mint_order_submission_tokens fksanmx7o4e9ptca64ynrgxufcy; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.mint_order_submission_tokens
    ADD CONSTRAINT fksanmx7o4e9ptca64ynrgxufcy FOREIGN KEY (mint_order_submission_id) REFERENCES public.mint_order_submission(id);


--
-- Name: transaction fksgq0ufsb102xtb0i9tbplaggy; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT fksgq0ufsb102xtb0i9tbplaggy FOREIGN KEY (account_key) REFERENCES public.account(key);


--
-- Name: account_funding_addresses_history fktq4iwr8leeim6oa9qfgj3vhsi; Type: FK CONSTRAINT; Schema: public; Owner: peter
--

ALTER TABLE ONLY public.account_funding_addresses_history
    ADD CONSTRAINT fktq4iwr8leeim6oa9qfgj3vhsi FOREIGN KEY (account_key) REFERENCES public.account(key);


--
-- PostgreSQL database dump complete
--

