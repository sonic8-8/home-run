-- Platform user account and authentication linkage.
create table if not exists users (
  user_id integer generated always as identity primary key,
  email varchar(255) not null,
  user_name varchar(100) not null,
  nickname varchar(100),
  password_hash varchar(255) not null,
  auth_provider_type varchar(20) not null,
  ssafy_user_key varchar(255),
  ssafy_connected_at timestamp,
  account_auth_verified_at timestamp,
  payment_type varchar(100), -- 회원 주요 결제 분야
  created_at timestamp not null default current_timestamp,
  constraint uq_users__email unique (email),
  constraint uq_users__ssafy_user_key unique (ssafy_user_key)
);

-- PASS product catalog for savings challenges.
create table if not exists pass_products (
  pass_product_id integer generated always as identity primary key,
  pass_product_name varchar(100) not null,
  pass_product_description text,
  default_saving_amount integer
);

-- Per-user aggregated PASS savings summary.
create table if not exists pass_savings (
  user_id integer primary key,
  monthly_saved_amount integer,
  total_saved_amount integer,
  updated_at timestamp not null default current_timestamp,
  constraint fk_pass_savings__user
    foreign key (user_id) references users (user_id)
);

-- User subscriptions to PASS products and linked funding source.
create table if not exists pass_subscriptions (
  pass_subscription_id integer generated always as identity primary key,
  user_id integer not null,
  pass_product_id integer not null,
  pass_product_name varchar(100),
  amount_per_save integer,
  source_account_reference varchar(100),
  active_yn boolean not null default true,
  subscribed_at timestamp,
  canceled_at timestamp,
  constraint fk_pass_subscriptions__user
    foreign key (user_id) references users (user_id),
  constraint fk_pass_subscriptions__pass_product
    foreign key (pass_product_id) references pass_products (pass_product_id)
);

-- Transaction history created from PASS save actions.
create table if not exists user_pass_transactions (
  user_pass_transaction_id integer generated always as identity primary key,
  pass_subscription_id integer not null,
  transaction_type varchar(30),
  transaction_amount integer,
  transaction_at timestamp,
  constraint fk_user_pass_transactions__pass_subscription
    foreign key (pass_subscription_id) references pass_subscriptions (pass_subscription_id)
);

-- Seedmoney account snapshot used by the service.
create table if not exists seedmoney_accounts (
  seedmoney_account_id integer generated always as identity primary key,
  user_id integer not null,
  bank_name varchar(100),
  account_number_masked varchar(50),
  balance_snapshot_amount integer,
  updated_at timestamp not null default current_timestamp,
  constraint fk_seedmoney_accounts__user
    foreign key (user_id) references users (user_id)
);

-- Deposit, transfer, and save transaction history for seedmoney.
create table if not exists seedmoney_transactions (
  seedmoney_transaction_id integer generated always as identity primary key,
  user_id integer not null,
  pass_subscription_id integer,
  transaction_type varchar(30),
  amount integer,
  counterparty_account_masked varchar(50),
  created_at timestamp not null default current_timestamp,
  constraint fk_seedmoney_transactions__user
    foreign key (user_id) references users (user_id),
  constraint fk_seedmoney_transactions__pass_subscription
    foreign key (pass_subscription_id) references pass_subscriptions (pass_subscription_id)
);

-- User card payment history used for card recommendation scoring.
create table if not exists member_payment_histories (
  payment_history_id integer generated always as identity primary key,
  user_id integer not null,
  category_id varchar(50) not null,
  category_name varchar(50) not null,
  merchant_name varchar(100) not null,
  payment_amount integer not null,
  payment_date date not null,
  created_at timestamp not null default current_timestamp,
  constraint fk_member_payment_histories__user
    foreign key (user_id) references users (user_id)
);

-- Static real-estate listing master data shared across sessions.
create table if not exists housing_regions (
  region_code varchar(30) primary key,
  region_name varchar(100)
);

create table if not exists housing_districts (
  district_code varchar(30) primary key,
  region_code varchar(30),
  district_name varchar(100),
  district_legal_dong_code varchar(30)
);

create table if not exists housing_legal_dongs (
  legal_dong_code varchar(30) primary key,
  parent_legal_dong_code varchar(30),
  region_code varchar(30),
  district_code varchar(30),
  legal_dong_name varchar(100),
  full_address_name varchar(255)
);

create table if not exists apartment_trade_raws (
  apartment_trade_raw_id integer generated always as identity primary key,
  trade_key varchar(255) not null,
  district_code varchar(30),
  legal_dong_name varchar(100),
  legal_dong_code varchar(30),
  apartment_name varchar(200),
  jibun varchar(100),
  deal_date date,
  deal_amount numeric(19,0),
  exclusive_area numeric(10,2),
  floor integer,
  build_year integer,
  land_leasehold boolean,
  constraint uq_apartment_trade_raws__trade_key unique (trade_key)
);

create table if not exists real_estate_geocode_caches (
  real_estate_geocode_cache_id integer generated always as identity primary key,
  geocoding_query varchar(255) not null,
  geocoding_status varchar(30) not null,
  latitude numeric(10,7),
  longitude numeric(10,7),
  resolved_road_address varchar(255),
  resolved_jibun_address varchar(255),
  constraint uq_real_estate_geocode_caches__geocoding_query unique (geocoding_query)
);

create table if not exists real_estate_properties (
  property_id bigint generated always as identity primary key,
  provider_id varchar(255),
  property_name varchar(200),
  address varchar(255),
  region_code varchar(30),
  district_code varchar(30),
  legal_dong_code varchar(30),
  base_price_amount numeric(19,0),
  latitude numeric(10,7),
  longitude numeric(10,7),
  housing_type varchar(50),
  property_type varchar(50),
  transaction_type varchar(50),
  contract_traps jsonb,
  constraint uq_real_estate_properties__provider_id unique (provider_id)
);

-- Top-level game session aggregate for one save slot.
create table if not exists game_sessions (
  game_session_id bigint generated always as identity primary key,
  user_id integer not null,
  slot_number integer not null,
  character_name varchar(100) not null,
  character_type varchar(20) not null,
  job_type varchar(50) not null,
  housing_type varchar(50) not null,
  region_code varchar(30) not null,
  district_code varchar(30) not null,
  target_property_id bigint not null,
  data_source_type varchar(20) not null,
  current_turn integer not null,
  "current_date" date,
  cycle_phase varchar(50),
  cash_balance_amount numeric(19,0) not null,
  net_worth_amount numeric(19,0) not null,
  session_status varchar(20) not null,
  created_at timestamp not null default current_timestamp,
  last_played_at timestamp,
  selected_card_monthly_saving_amount integer not null default 0,
  owned_property_id bigint,
  constraint fk_game_sessions__user
    foreign key (user_id) references users (user_id),
  constraint fk_game_sessions__target_property
    foreign key (target_property_id) references real_estate_properties (property_id),
  constraint fk_game_sessions__owned_property
    foreign key (owned_property_id) references real_estate_properties (property_id),
  constraint uq_game_sessions__user_id__slot_number
    unique (user_id, slot_number)
);

-- Current core character stats for a game session.
create table if not exists game_stats (
  game_session_id bigint primary key,
  health integer not null,
  fatigue integer not null,
  stress integer not null,
  knowledge integer,
  happiness integer,
  burnout_yn boolean,
  burnout_started_turn integer,
  hospitalization_ends_turn integer,
  constraint fk_game_stats__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);

-- Career and employment progression state for a game session.
create table if not exists game_careers (
  game_session_id bigint primary key,
  job_type varchar(50),
  job_title varchar(100),
  salary_amount integer,
  tenure_turns integer,
  recent_12_turn_study_count integer,
  recent_12_turn_networking_count integer,
  negotiation_preparation_score integer,
  last_salary_negotiation_turn integer,
  employment_status varchar(20),
  probation_ends_turn integer,
  reemployment_available_turn integer,
  unemployment_benefit_remaining_turns integer,
  pre_resignation_salary_amount integer,
  constraint fk_game_careers__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);

-- Selectable action master list used when filling turn slots.
create table if not exists action_masters (
  action_type varchar(30) primary key,
  action_category varchar(20) not null,
  action_name varchar(100) not null,
  description text,
  active_yn boolean not null default true
);

-- Actual action selections for each session, turn, and slot.
create table if not exists game_turn_slots (
  game_turn_slot_id integer generated always as identity primary key,
  game_session_id bigint not null,
  turn_number integer not null,
  slot_index integer not null,
  action_type varchar(30) not null,
  action_category varchar(20),
  forced_action_yn boolean not null default false,
  constraint fk_game_turn_slots__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_turn_slots__action_master
    foreign key (action_type) references action_masters (action_type),
  constraint uq_game_turn_slots__game_session_id__turn_number__slot_index
    unique (game_session_id, turn_number, slot_index)
);

-- Turn settlement logs describing what changed and why.
create table if not exists settlement_logs (
  settlement_log_id integer generated always as identity primary key,
  game_session_id bigint not null,
  turn_number integer not null,
  settlement_phase_type varchar(50),
  description text,
  cash_change_amount integer,
  stat_changes jsonb,
  constraint fk_settlement_logs__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);

-- Current housing state for a game session.
create table if not exists game_housings (
  game_session_id bigint primary key,
  current_housing_type varchar(50),
  current_deposit_amount numeric(19,0),
  monthly_rent_amount numeric(19,0),
  maintenance_fee_amount numeric(19,0),
  current_property_id bigint,
  constraint fk_game_housings__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_housings__current_property
    foreign key (current_property_id) references real_estate_properties (property_id)
);

-- Session-scoped real-estate market prices derived from cycle/news updates.
create table if not exists game_property_market_states (
  game_session_id bigint not null,
  property_id bigint not null,
  current_price_amount integer not null,
  last_updated_turn integer not null,
  primary key (game_session_id, property_id),
  constraint fk_game_property_market_states__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_property_market_states__property
    foreign key (property_id) references real_estate_properties (property_id)
);

-- Contract and registry documents attached to a real-estate listing.
create table if not exists real_estate_documents (
  real_estate_document_id integer generated always as identity primary key,
  property_id bigint not null,
  document_type varchar(20),
  image_url varchar(255), -- T26 added: document viewer image path
  checklist jsonb, -- T26 added: contract review checklist items
  registry_section varchar(20),
  quiz_sample_payload jsonb,
  constraint fk_real_estate_documents__property
    foreign key (property_id) references real_estate_properties (property_id)
);

-- Player review results for property contract inspection.
create table if not exists game_contract_reviews (
  game_contract_review_id integer generated always as identity primary key,
  game_session_id bigint not null,
  property_id bigint not null,
  review_status varchar(20),
  checked_traps jsonb,
  detected_traps jsonb,
  contract_result_type varchar(20),
  reviewed_at timestamp,
  constraint fk_game_contract_reviews__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_contract_reviews__property
    foreign key (property_id) references real_estate_properties (property_id)
);

-- Static stock master data shared across sessions.
create table if not exists stock_markets (
  stock_code varchar(20) primary key,
  stock_name varchar(100) not null,
  kis_stock_code varchar(10),             -- 한투 OpenAPI 종목코드 (예: '005930')
  sector varchar(100),
  base_price_amount integer,
  volatility_rate numeric(8,4)
);

-- Session-scoped current stock prices used for valuation and execution.
create table if not exists game_stock_market_states (
  game_session_id integer not null,
  stock_code varchar(20) not null,
  current_price_amount integer not null,
  last_updated_turn integer not null,
  primary key (game_session_id, stock_code),
  constraint fk_game_stock_market_states__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_stock_market_states__stock_market
    foreign key (stock_code) references stock_markets (stock_code)
);

-- Current stock holdings owned by a game session.
create table if not exists stock_holdings (
  game_session_id integer not null,
  stock_code varchar(20) not null,
  average_purchase_price_amount integer,
  quantity integer,
  primary key (game_session_id, stock_code),
  constraint fk_stock_holdings__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_stock_holdings__stock_market
    foreign key (stock_code) references stock_markets (stock_code)
);

-- Buy and sell orders waiting for or having completed execution.
create table if not exists stock_orders (
  stock_order_id integer generated always as identity primary key,
  game_session_id integer not null,
  stock_code varchar(20) not null,
  order_type varchar(20),
  quantity integer,
  ordered_turn integer,
  execute_turn integer,
  order_status varchar(20),
  constraint fk_stock_orders__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_stock_orders__stock_market
    foreign key (stock_code) references stock_markets (stock_code)
);

-- Loan screening/application workflow state per session.
create table if not exists loan_applications (
  loan_application_id integer generated always as identity primary key,
  game_session_id integer not null,
  loan_type varchar(20),                  -- 'CREDIT', 'JEONSE', 'MORTGAGE'
  product_id varchar(100),
  property_id integer,                    -- nullable: 개인신용대출은 매물 불필요
  application_status varchar(20),
  approved_limit_amount integer,
  rejection_reason text,
  applied_at timestamp,
  confirmed_at timestamp,
  constraint fk_loan_applications__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_loan_applications__property
    foreign key (property_id) references real_estate_properties (property_id)
);

-- Active loans held inside the game session.
create table if not exists game_loans (
  game_loan_id integer generated always as identity primary key,
  game_session_id integer not null,
  loan_name varchar(100),
  principal_amount integer,
  interest_rate numeric(8,4),
  monthly_payment_amount integer,
  remaining_repayment_turns integer,
  product_id varchar(100),
  repayment_type varchar(30),
  loan_status varchar(20),
  constraint fk_game_loans__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);

-- Static card product catalog shared across recommendation and registration flows.
-- card_image_url stores the OCI Object Storage object name, not a full public URL.
-- Example: card-kb-my-wesh-front.png -> GET /api/v1/images?objectName={card_image_url}
create table if not exists card_products (
  card_product_id integer generated always as identity primary key,
  card_name varchar(100) not null,
  card_issuer_name varchar(100),
  card_description text,
  baseline_performance_amount integer,
  max_benefit_limit_amount integer,
  active_benefits jsonb,
  card_image_url varchar(255),
  active_yn boolean not null default true
);

-- Session-scoped card selections referencing a real card product.
-- card_status_type manages both recommended and registered cards in one table.
create table if not exists game_cards (
  game_card_id integer generated always as identity primary key,
  game_session_id integer not null,
  card_product_id integer not null,
  card_status_type varchar(20) not null, -- RECOMMENDED or REGISTERED
  recommended_at timestamp,
  registered_at timestamp,
  active_yn boolean not null default true,
  constraint fk_game_cards__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_cards__card_product
    foreign key (card_product_id) references card_products (card_product_id)
);

-- Static news master data with sector, property, and job impacts.
create table if not exists news_master (
  news_id varchar(100) primary key,
  title varchar(255),
  category varchar(100),
  sentiment varchar(50),
  sector_impact jsonb,
  exchange_rate_impact integer,
  real_estate_impact integer,
  job_impact jsonb
);

-- News exposed to a session on a given turn.
create table if not exists game_news_logs (
  game_news_log_id integer generated always as identity primary key,
  game_session_id integer not null,
  turn_number integer not null,
  news_id varchar(100) not null,
  headline_snapshot varchar(255),
  published_date date,
  constraint fk_game_news_logs__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_news_logs__news
    foreign key (news_id) references news_master (news_id)
);

-- Event master defining trigger and presentation metadata.
create table if not exists game_events (
  game_event_id integer generated always as identity primary key,
  event_type_code varchar(50) not null,
  event_code varchar(100) not null unique,
  event_name varchar(100) not null,
  event_presentation_type varchar(50) not null,
  event_trigger_type varchar(50) not null,
  event_trigger_value numeric(15,4),
  choice_required_yn boolean not null default false,
  image_url varchar(255),
  sender_name varchar(255),
  receiver_name varchar(255),
  description text,
  active_yn boolean not null default true,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp
);

-- Detail payload for voice-phishing events.
create table if not exists voice_phishing_event_details (
  voice_phishing_event_id integer primary key,
  base_damage_amount integer,
  constraint fk_voice_phishing_event_details__game_event
    foreign key (voice_phishing_event_id) references game_events (game_event_id)
);

-- Detail payload for overtime request events.
create table if not exists overtime_request_event_details (
  overtime_request_event_id integer primary key,
  boom_occurrence_rate numeric(5,4),
  normal_occurrence_rate numeric(5,4),
  accept_reward_amount integer,
  accept_stress_change_amount integer,
  constraint fk_overtime_request_event_details__game_event
    foreign key (overtime_request_event_id) references game_events (game_event_id)
);

-- Detail payload for rent increase notice events.
create table if not exists rent_increase_notice_event_details (
  rent_increase_notice_event_id integer primary key,
  increase_value_type varchar(50),
  increase_base_value integer,
  moving_choice_allowed_yn boolean not null,
  constraint fk_rent_increase_notice_event_details__game_event
    foreign key (rent_increase_notice_event_id) references game_events (game_event_id)
);

-- Detail payload for appliance breakdown events.
create table if not exists appliance_breakdown_event_details (
  appliance_breakdown_event_id integer primary key,
  repair_cost_amount integer,
  replacement_cost_amount integer,
  replacement_stress_change_amount integer,
  constraint fk_appliance_breakdown_event_details__game_event
    foreign key (appliance_breakdown_event_id) references game_events (game_event_id)
);

-- Detail payload for family/ceremony expense events.
create table if not exists family_event_details (
  family_event_id integer primary key,
  attendance_cost_amount integer,
  attendance_happiness_change_amount integer,
  constraint fk_family_event_details__game_event
    foreign key (family_event_id) references game_events (game_event_id)
);

-- Detail payload for burnout events.
create table if not exists burnout_event_details (
  burnout_event_id integer primary key,
  next_turn_available_slot_count integer,
  forced_rest_slot_count integer,
  knowledge_gain_blocked_yn boolean not null,
  release_condition_description text,
  constraint fk_burnout_event_details__game_event
    foreign key (burnout_event_id) references game_events (game_event_id)
);

-- Detail payload for hospitalization events.
create table if not exists hospitalization_event_details (
  hospitalization_event_id integer primary key,
  income_block_min_turns integer,
  income_block_max_turns integer,
  constraint fk_hospitalization_event_details__game_event
    foreign key (hospitalization_event_id) references game_events (game_event_id)
);

-- Detail payload for forced resignation events.
create table if not exists forced_resignation_event_details (
  forced_resignation_event_id integer primary key,
  unemployment_benefit_payment_rate numeric(5,4),
  unemployment_benefit_max_payment_turns integer,
  constraint fk_forced_resignation_event_details__game_event
    foreign key (forced_resignation_event_id) references game_events (game_event_id)
);

-- Detail payload for job transfer offer events.
create table if not exists job_transfer_event_details (
  job_transfer_event_id integer primary key,
  offered_salary_min_multiplier numeric(5,4),
  offered_salary_max_multiplier numeric(5,4),
  probation_min_turns integer,
  probation_max_turns integer,
  tenure_reset_yn boolean not null,
  constraint fk_job_transfer_event_details__game_event
    foreign key (job_transfer_event_id) references game_events (game_event_id)
);

-- Detail payload for real-estate regulation events.
create table if not exists real_estate_regulation_event_details (
  real_estate_regulation_event_id integer primary key,
  impact_rate numeric(8,4),
  duration_turns integer,
  constraint fk_real_estate_regulation_event_details__game_event
    foreign key (real_estate_regulation_event_id) references game_events (game_event_id)
);

-- Detail payload for rate-change events.
create table if not exists rate_change_event_details (
  rate_change_event_id integer primary key,
  change_rate numeric(8,4),
  duration_turns integer,
  constraint fk_rate_change_event_details__game_event
    foreign key (rate_change_event_id) references game_events (game_event_id)
);

-- Detail payload for hiring freeze events.
create table if not exists hiring_freeze_event_details (
  hiring_freeze_event_id integer primary key,
  additional_reemployment_wait_turns integer,
  constraint fk_hiring_freeze_event_details__game_event
    foreign key (hiring_freeze_event_id) references game_events (game_event_id)
);

-- Selectable choices for a specific game event.
create table if not exists event_choices (
  event_choice_id integer generated always as identity primary key,
  game_event_id integer not null,
  choice_code varchar(50) not null,
  choice_name varchar(100) not null,
  choice_order integer not null,
  choice_description text,
  constraint fk_event_choices__game_event
    foreign key (game_event_id) references game_events (game_event_id),
  constraint uq_event_choices__game_event_id__choice_code
    unique (game_event_id, choice_code),
  constraint uq_event_choices__game_event_id__choice_order
    unique (game_event_id, choice_order)
);

-- Conditional rules that determine whether an event can trigger.
create table if not exists event_conditions (
  event_condition_id integer generated always as identity primary key,
  game_event_id integer not null,
  condition_group_number integer,
  condition_order integer not null,
  condition_type varchar(50) not null,
  target_table_name varchar(100),
  target_column_name varchar(100),
  comparison_operator varchar(20),
  criteria_text_value varchar(255),
  criteria_number_value_1 numeric(15,4),
  criteria_number_value_2 numeric(15,4),
  logical_operator_type varchar(20),
  constraint fk_event_conditions__game_event
    foreign key (game_event_id) references game_events (game_event_id)
);

-- Effects applied by an event or by a specific event choice.
create table if not exists event_effects (
  event_effect_id integer generated always as identity primary key,
  game_event_id integer not null,
  event_choice_id integer,
  effect_order integer not null,
  application_timing_type varchar(50) not null,
  target_table_name varchar(100),
  target_column_name varchar(100),
  operation_type varchar(50),
  base_number_value integer,
  min_number_value integer,
  max_number_value integer,
  base_text_value varchar(255),
  duration_turns integer,
  note text,
  constraint fk_event_effects__game_event
    foreign key (game_event_id) references game_events (game_event_id),
  constraint fk_event_effects__event_choice
    foreign key (event_choice_id) references event_choices (event_choice_id)
);

-- Unresolved events queued for the player in a session.
create table if not exists game_pending_events (
  game_pending_event_id integer generated always as identity primary key,
  game_session_id integer not null,
  turn_number integer not null,
  game_event_id integer not null,
  event_presentation_type varchar(50) not null,
  payload jsonb,
  resolved_yn boolean not null default false,
  created_at timestamp not null default current_timestamp,
  constraint fk_game_pending_events__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_pending_events__game_event
    foreign key (game_event_id) references game_events (game_event_id)
);

-- Resolved event history including chosen option and results.
create table if not exists game_event_logs (
  game_event_log_id integer generated always as identity primary key,
  game_session_id integer not null,
  turn_number integer not null,
  game_event_id integer not null,
  event_choice_id integer,
  selected_choice_code varchar(50),
  result_effects jsonb,
  result_summary text,
  resolved_at timestamp,
  constraint fk_game_event_logs__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_event_logs__game_event
    foreign key (game_event_id) references game_events (game_event_id),
  constraint fk_game_event_logs__event_choice
    foreign key (event_choice_id) references event_choices (event_choice_id)
);

-- Fine-grained audit history of important game state changes.
create table if not exists game_play_histories (
  game_play_history_id integer generated always as identity primary key,
  game_session_id integer not null,
  game_event_id integer,
  table_name varchar(100),
  column_name varchar(100),
  changed_table_primary_key_1 varchar(100),
  changed_table_primary_key_2 varchar(100),
  changed_table_primary_key_3 varchar(100),
  before_data text,
  after_data text,
  started_at timestamp,
  ended_at timestamp,
  event_choice_id integer,
  result_effects jsonb,
  result_summary text,
  occurred_turn_number integer,
  constraint fk_game_play_histories__game_session
    foreign key (game_session_id) references game_sessions (game_session_id),
  constraint fk_game_play_histories__game_event
    foreign key (game_event_id) references game_events (game_event_id),
  constraint fk_game_play_histories__event_choice
    foreign key (event_choice_id) references event_choices (event_choice_id)
);

-- Final report snapshot generated when a game session ends.
create table if not exists game_reports (
  game_session_id integer primary key,
  ending_type varchar(20),
  ending_title varchar(255),
  total_income_amount integer,
  total_expense_amount integer,
  investment_yield_rate integer,
  play_tags jsonb,
  summary_text text,
  grade varchar(5),
  total_assets_amount integer,
  net_profit_amount integer,
  top_spending_category varchar(100),
  top_spending_ratio numeric(5,2),
  achievements jsonb,
  constraint fk_game_reports__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);

-- Per-turn timeline snapshots for charts and ending summaries.
create table if not exists game_timelines (
  game_timeline_id integer generated always as identity primary key,
  game_session_id integer not null,
  turn_number integer not null,
  logged_date date,
  cash integer,
  net_assets integer,
  total_assets integer,
  stock_value_amount integer,
  loan_balance_amount integer,
  salary_amount integer,
  constraint fk_game_timelines__game_session
    foreign key (game_session_id) references game_sessions (game_session_id)
);
