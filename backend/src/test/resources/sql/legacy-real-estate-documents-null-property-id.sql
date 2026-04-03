create table if not exists real_estate_documents (
  real_estate_document_id bigserial primary key,
  property_id bigint,
  document_type varchar(255),
  registry_section varchar(255),
  image_url varchar(255),
  checklist jsonb,
  quiz_sample_payload jsonb
);

insert into real_estate_documents (
  property_id,
  document_type,
  registry_section,
  image_url,
  checklist,
  quiz_sample_payload
) values (
  null,
  'REGISTRY',
  'GAPGU',
  null,
  '[]'::jsonb,
  '{
    "quizVerdict":"정상",
    "rows":[
      {
        "rank_no":"1",
        "purpose":"소유권보존",
        "receipt":"2024년 1월 1일",
        "reason":"보존",
        "details":"legacy gapgu sample",
        "rendering":{}
      }
    ],
    "issueSummary":"legacy gapgu sample",
    "keyPoints":["legacy point"],
    "feedbackCorrect":"legacy correct",
    "feedbackWrong":"legacy wrong"
  }'::jsonb
);

insert into real_estate_documents (
  property_id,
  document_type,
  registry_section,
  image_url,
  checklist,
  quiz_sample_payload
) values (
  null,
  'REGISTRY',
  'EULGU',
  null,
  '[]'::jsonb,
  '{
    "quizVerdict":"위험",
    "rows":[
      {
        "rank_no":"1",
        "purpose":"근저당권설정",
        "receipt":"2024년 2월 1일",
        "reason":"설정계약",
        "details":"legacy eulgu sample",
        "rendering":{}
      }
    ],
    "issueSummary":"legacy eulgu sample",
    "keyPoints":["legacy eulgu point"],
    "feedbackCorrect":"legacy eulgu correct",
    "feedbackWrong":"legacy eulgu wrong"
  }'::jsonb
);
