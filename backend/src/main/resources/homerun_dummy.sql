-- Seed data for homerun tables.
-- active_benefits jsonb shape:
-- [
--   {
--     "categoryId": "CG-9ca85f66311a23d",
--     "categoryName": "생활",
--     "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
--     "discountRate": 12.0,
--     "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
--   }
-- ]
--
-- The category IDs and names follow SSAFY_금융망_API/카드.md.
-- Card metadata is maintained directly in this SQL file.

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

alter table if exists game_sessions
  add column if not exists selected_card_monthly_saving_amount integer not null default 0;

insert into users (
  email,
  user_name,
  password_hash,
  auth_provider_type
) values (
  'homerun@example.com',
  'homerun',
  '$2a$10$W.P/94ThsqwKVLp35TQW6uIVK3Q56JTQSqFBeHn82/l.Yzke7E8H.',
  'EMAIL'
);

insert into card_products (
  card_name,
  card_issuer_name,
  card_description,
  baseline_performance_amount,
  max_benefit_limit_amount,
  active_benefits,
  card_image_url,
  active_yn
) values
  (
    'KB국민 My WE:SH 카드',
    'KB국민카드',
    'KB Pay와 식음료, OTT, 통신을 중심으로 생활비 혜택을 넓게 묶은 선택형 생활 카드',
    400000,
    35000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "KB Pay 결제"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "일반 음식점",
                  "GS25",
                  "CU"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "SKT",
                  "KT",
                  "LG U+"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 30.0,
            "exampleMerchants": [
                  "넷플릭스",
                  "유튜브 프리미엄",
                  "티빙"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "배달의민족",
                  "요기요",
                  "커피전문점"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "택시"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "올리브영",
                  "교보문고",
                  "미용실"
            ]
      }
]',
    'card-kb-my-wesh-front.png',
    true
  ),
  (
    '굿데이올림카드',
    'KB국민카드',
    '주유와 장보기, 통신비, 교통까지 고정지출 전반을 줄이기 좋은 생활 실속 카드',
    300000,
    40000,
    '[
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 3.5,
            "exampleMerchants": [
                  "주유소",
                  "충전소",
                  "GS칼텍스"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "이마트",
                  "롯데마트",
                  "홈플러스"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "전화요금",
                  "인터넷",
                  "IPTV"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "버스",
                  "지하철",
                  "택시"
            ]
      },
      {
            "categoryId": "CG-8fa85f6425e1123",
            "categoryName": "해외",
            "categoryDescription": "(해외직구)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "해외직구",
                  "해외 가맹점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "음식점",
                  "커피전문점",
                  "편의점"
            ]
      },
      {
            "categoryId": "CG-6dd85f6425ez11o",
            "categoryName": "교육/육아",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "학원",
                  "독서실",
                  "종합스포츠센터"
            ]
      }
]',
    'card-kb-goodday-olim-front.png',
    true
  ),
  (
    'toss KB국민카드',
    'KB국민카드',
    '전 가맹점 기본 적립에 스타벅스 추가 적립을 얹은 심플 포인트 카드',
    0,
    25000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 4.0,
            "exampleMerchants": [
                  "스타벅스"
            ]
      }
]',
    'card-kb-toss-front.png',
    true
  ),
  (
    'KB국민 청춘대로 톡톡카드',
    'KB국민카드',
    '스타벅스와 간편결제, 대중교통에 강한 청년층 생활 할인 카드',
    300000,
    30000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 50.0,
            "exampleMerchants": [
                  "스타벅스"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 20.0,
            "exampleMerchants": [
                  "버거·패스트푸드 업종"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "삼성페이",
                  "네이버페이",
                  "카카오페이"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "버스",
                  "지하철",
                  "택시"
            ]
      }
]',
    'card-kb-cheongchun-toktok-front.png',
    true
  ),
  (
    'KB국민 톡톡 my point 카드',
    'KB국민카드',
    '전월 실적 없이 국내 결제와 KB Pay 적립에 집중한 심플 포인트 카드',
    300000,
    30000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.5,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "KB Pay 결제"
            ]
      }
]',
    'card-kb-toktok-my-point-front.png',
    true
  ),
  (
    '삼성카드 taptap O',
    '삼성카드',
    '스타벅스·쇼핑 패키지 선택과 교통·통신 할인 조합이 강한 선택형 카드',
    300000,
    40000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 50.0,
            "exampleMerchants": [
                  "스타벅스"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 7.0,
            "exampleMerchants": [
                  "G마켓",
                  "옥션",
                  "11번가"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "버스",
                  "지하철",
                  "택시"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "SKT",
                  "KT",
                  "LG U+"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 50.0,
            "exampleMerchants": [
                  "CGV",
                  "롯데시네마"
            ]
      }
]',
    'card-samsung-taptap-o-front.png',
    true
  ),
  (
    '삼성 iD ON 카드',
    '삼성카드',
    '커피·배달 등 많이 쓰는 생활 영역과 간편결제에 집중한 자동맞춤 할인 카드',
    300000,
    40000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 30.0,
            "exampleMerchants": [
                  "스타벅스",
                  "배달의민족",
                  "쉐이크쉑"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "버스",
                  "지하철",
                  "택시"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 3.0,
            "exampleMerchants": [
                  "삼성페이",
                  "네이버페이",
                  "카카오페이"
            ]
      }
]',
    'card-samsung-id-on-front.png',
    true
  ),
  (
    '삼성 iD ALL 카드',
    '삼성카드',
    '쇼핑 영역 자동 맞춤 할인에 주유·통신 정기지출을 더한 범용 카드',
    500000,
    50000,
    '[
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "신세계백화점",
                  "이마트",
                  "롯데슈퍼"
            ]
      },
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 2.5,
            "exampleMerchants": [
                  "주유소",
                  "LPG충전소",
                  "전기차충전소"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.5,
            "exampleMerchants": [
                  "국내외 전 가맹점"
            ]
      }
]',
    'card-samsung-id-all-front.png',
    true
  ),
  (
    '삼성카드 & MILEAGE PLATINUM',
    '삼성카드',
    '기본 마일리지 적립에 해외 또는 국내 선택형 추가 적립을 얹는 여행형 카드',
    500000,
    60000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 2.0,
            "exampleMerchants": [
                  "SK에너지",
                  "GS칼텍스",
                  "S-OIL"
            ]
      },
      {
            "categoryId": "CG-8fa85f6425e1123",
            "categoryName": "해외",
            "categoryDescription": "(해외직구)",
            "discountRate": 2.0,
            "exampleMerchants": [
                  "해외 가맹점",
                  "해외 직접구매"
            ]
      }
]',
    'card-samsung-mileage-platinum-front.png',
    true
  ),
  (
    '삼성 iD VITA 카드',
    '삼성카드',
    '의료·보험·헬스케어와 장보기까지 챙기는 가족형 생활비 카드',
    300000,
    35000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 20.0,
            "exampleMerchants": [
                  "병·의원",
                  "약국"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "생명보험",
                  "손해보험"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 20.0,
            "exampleMerchants": [
                  "아모레몰",
                  "초록마을",
                  "헬스케어관"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "이마트",
                  "트레이더스",
                  "홈플러스"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "SKT",
                  "KT",
                  "LG U+"
            ]
      },
      {
            "categoryId": "CG-8fa85f6425e1123",
            "categoryName": "해외",
            "categoryDescription": "(해외직구)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "해외 가맹점",
                  "해외 직접구매"
            ]
      }
]',
    'card-samsung-id-vita-front.png',
    true
  ),
  (
    '디지로카 London',
    '롯데카드',
    '전 가맹점 캐시백과 빠른 상환 추가 캐시백에 집중한 심플 범용 카드',
    700000,
    130000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.7,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "즉시결제 대상 일시불"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "Weekly 자동결제 대상 일시불"
            ]
      }
]',
    'card-lotte-digiroca-london-front.png',
    true
  ),
  (
    'LOCA LIKIT 1.2',
    '롯데카드',
    '실적 없이 국내외 기본 할인에 온라인 가맹점 우대가 붙는 심플 할인 카드',
    0,
    30000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.2,
            "exampleMerchants": [
                  "국내외 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 1.5,
            "exampleMerchants": [
                  "온라인 쇼핑 가맹점"
            ]
      }
]',
    'card-lotte-loca-likit-1-2-front.png',
    true
  ),
  (
    'LOCA 365 카드',
    '롯데카드',
    '관리비와 공과금, 통신, 배달처럼 월 고정비를 폭넓게 할인하는 생활비 카드',
    500000,
    36000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "아파트관리비"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "전기료",
                  "도시가스"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "지하철",
                  "시내버스"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "SKT",
                  "KT",
                  "LG U+"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "배달의민족",
                  "요기요",
                  "쿠팡이츠"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "생명보험",
                  "손해보험"
            ]
      },
      {
            "categoryId": "CG-6dd85f6425ez11o",
            "categoryName": "교육/육아",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "학습지"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 15.0,
            "exampleMerchants": [
                  "넷플릭스",
                  "유튜브",
                  "디즈니 플러스"
            ]
      }
]',
    'card-lotte-loca-365-front.png',
    true
  ),
  (
    '디지로카 Paris',
    '롯데카드',
    '온라인 쇼핑 멤버십 연동 추가 할인에 강한 이커머스 특화 카드',
    500000,
    70000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.7,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "쿠팡",
                  "네이버페이",
                  "11번가"
            ]
      }
]',
    'card-lotte-digiroca-paris-front.png',
    true
  ),
  (
    'LOCA Professional',
    '롯데카드',
    '전 가맹점 캐시백과 상환 편의 혜택 중심의 프리미엄 범용 카드',
    800000,
    100000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "즉시결제 대상 일시불"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "Weekly 자동결제 대상 일시불"
            ]
      }
]',
    'card-lotte-loca-professional-front.png',
    true
  ),
  (
    '신한카드 Mr.Life',
    '신한카드',
    '공과금과 장보기, 야간 생활비 지출에 강한 대표 생활비 할인 카드',
    300000,
    40000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "전기요금",
                  "도시가스",
                  "통신요금"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "편의점"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "병원",
                  "약국"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "세탁소"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "옥션",
                  "G마켓",
                  "11번가"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "택시"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "음식점",
                  "커피전문점"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 10.0,
            "exampleMerchants": [
                  "이마트",
                  "롯데마트",
                  "홈플러스"
            ]
      },
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 3.5,
            "exampleMerchants": [
                  "SK에너지",
                  "GS칼텍스",
                  "S-Oil"
            ]
      }
]',
    'card-shinhan-mr-life-front.png',
    true
  ),
  (
    '신한카드 Point Plan',
    '신한카드',
    '건당 적립률과 외식·자동납부 보너스를 함께 챙기는 생활비 포인트 카드',
    400000,
    50000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 3.0,
            "exampleMerchants": [
                  "국내 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-8fa85f6425e1123",
            "categoryName": "해외",
            "categoryDescription": "(해외직구)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "해외 결제"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "음식점",
                  "배달의민족",
                  "요기요"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "도시가스",
                  "전기요금"
            ]
      }
]',
    'card-shinhan-point-plan-front.png',
    true
  ),
  (
    '신한 TRAVEL 카드',
    '신한카드',
    '해외 교통과 여행지 생활 소비에 특화된 트래블 혜택 카드',
    100000,
    100000,
    '[
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "해외 버스",
                  "지하철",
                  "트램"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "GS25",
                  "CU",
                  "세븐일레븐"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "시내버스",
                  "지하철",
                  "공항철도"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "FamilyMart",
                  "Lawson",
                  "Seven-Eleven"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "베트남 롯데마트"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "베트남 Grab"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "미국 스타벅스"
            ]
      }
]',
    'card-shinhan-travel-front.png',
    true
  ),
  (
    '신한카드 RPM+ Platinum#',
    '신한카드',
    '주유를 중심으로 쇼핑·정비·택시 적립을 묶은 차량 특화 포인트 카드',
    500000,
    50000,
    '[
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 4.7,
            "exampleMerchants": [
                  "SK에너지",
                  "GS칼텍스",
                  "S-Oil"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "롯데백화점",
                  "11번가",
                  "G마켓"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "롯데마트",
                  "이마트",
                  "홈플러스"
            ]
      },
      {
            "categoryId": "CG-4fa85f6455cad4a",
            "categoryName": "교통",
            "categoryDescription": "(버스, 지하철, 택시)",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "택시"
            ]
      },
      {
            "categoryId": "CG-3fa85f6425e811e",
            "categoryName": "주유",
            "categoryDescription": "",
            "discountRate": 5.0,
            "exampleMerchants": [
                  "차량 정비소",
                  "타이어샵"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 2.0,
            "exampleMerchants": [
                  "국내외 전 가맹점"
            ]
      }
]',
    'card-shinhan-rpm-plus-front.png',
    true
  ),
  (
    '신한카드 Deep Dream',
    '신한카드',
    '무실적 기본 적립에 자주 쓰는 DREAM 영역 추가 적립을 얹는 범용 포인트 카드',
    300000,
    45000,
    '[
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.2,
            "exampleMerchants": [
                  "국내외 전 가맹점"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 0.6,
            "exampleMerchants": [
                  "이마트",
                  "홈플러스",
                  "롯데마트"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.6,
            "exampleMerchants": [
                  "CU",
                  "GS25",
                  "올리브영"
            ]
      },
      {
            "categoryId": "CG-9ca85f66311a23d",
            "categoryName": "생활",
            "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
            "discountRate": 0.6,
            "exampleMerchants": [
                  "CGV",
                  "롯데시네마",
                  "커피전문점"
            ]
      },
      {
            "categoryId": "CG-8fa85f6425e1123",
            "categoryName": "해외",
            "categoryDescription": "(해외직구)",
            "discountRate": 0.6,
            "exampleMerchants": [
                  "해외 일시불"
            ]
      },
      {
            "categoryId": "CG-7fa85f6425bc311",
            "categoryName": "통신",
            "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
            "discountRate": 0.6,
            "exampleMerchants": [
                  "SKT",
                  "KT",
                  "LG U+"
            ]
      },
      {
            "categoryId": "CG-4fa85f6425ad1d3",
            "categoryName": "대형마트",
            "categoryDescription": "",
            "discountRate": 1.0,
            "exampleMerchants": [
                  "가장 많이 이용한 DREAM 영역"
            ]
      }
]',
    'card-shinhan-deep-dream-front.png',
    true
  );

delete from member_payment_histories
where user_id = (select user_id from users where email = 'homerun@example.com')
  and payment_date between date '2026-02-01' and date '2026-02-28';

insert into member_payment_histories (
  user_id,
  category_id,
  category_name,
  merchant_name,
  payment_amount,
  payment_date
)
select
  u.user_id,
  payments.category_id,
  payments.category_name,
  payments.merchant_name,
  payments.payment_amount,
  payments.payment_date
from users u
cross join (
  values
    ('CG-9ca85f66311a23d', '생활', '스타벅스', 28000, date '2026-02-02'),
    ('CG-9ca85f66311a23d', '생활', 'GS25', 22000, date '2026-02-03'),
    ('CG-9ca85f66311a23d', '생활', '맘스터치', 35000, date '2026-02-04'),
    ('CG-9ca85f66311a23d', '생활', '올리브영', 18000, date '2026-02-06'),
    ('CG-9ca85f66311a23d', '생활', '배달의민족', 26000, date '2026-02-07'),
    ('CG-9ca85f66311a23d', '생활', '이디야', 24000, date '2026-02-10'),
    ('CG-9ca85f66311a23d', '생활', 'CU', 31000, date '2026-02-12'),
    ('CG-9ca85f66311a23d', '생활', '파리바게뜨', 29000, date '2026-02-15'),
    ('CG-9ca85f66311a23d', '생활', '다이소', 33000, date '2026-02-18'),
    ('CG-9ca85f66311a23d', '생활', '메가MGC커피', 21000, date '2026-02-20'),
    ('CG-9ca85f66311a23d', '생활', '요기요', 27000, date '2026-02-23'),
    ('CG-9ca85f66311a23d', '생활', '교보문고', 26000, date '2026-02-26'),
    ('CG-4fa85f6455cad4a', '교통', '카카오T', 32000, date '2026-02-01'),
    ('CG-4fa85f6455cad4a', '교통', '카카오T', 24000, date '2026-02-08'),
    ('CG-4fa85f6455cad4a', '교통', '서울교통공사', 18000, date '2026-02-11'),
    ('CG-4fa85f6455cad4a', '교통', '카카오T', 41000, date '2026-02-17'),
    ('CG-4fa85f6455cad4a', '교통', '티머니', 35000, date '2026-02-24'),
    ('CG-4fa85f6425ad1d3', '대형마트', '이마트', 38000, date '2026-02-05'),
    ('CG-4fa85f6425ad1d3', '대형마트', '홈플러스', 22000, date '2026-02-09'),
    ('CG-4fa85f6425ad1d3', '대형마트', '롯데마트', 27000, date '2026-02-14'),
    ('CG-4fa85f6425ad1d3', '대형마트', '코스트코', 33000, date '2026-02-22'),
    ('CG-7fa85f6425bc311', '통신', 'SKT', 55000, date '2026-02-13'),
    ('CG-7fa85f6425bc311', '통신', 'KT 인터넷', 15000, date '2026-02-16'),
    ('CG-7fa85f6425bc311', '통신', 'LG U+ IPTV', 20000, date '2026-02-27'),
    ('CG-3fa85f6425e811e', '주유', 'GS칼텍스', 20000, date '2026-02-04'),
    ('CG-3fa85f6425e811e', '주유', 'SK에너지', 18000, date '2026-02-19'),
    ('CG-3fa85f6425e811e', '주유', 'S-OIL', 22000, date '2026-02-28'),
    ('CG-8fa85f6425e1123', '해외', '아마존', 17000, date '2026-02-18'),
    ('CG-8fa85f6425e1123', '해외', '알리익스프레스', 23000, date '2026-02-25'),
    ('CG-6dd85f6425ez11o', '교육/육아', '메가스터디', 20000, date '2026-02-21')
) as payments(category_id, category_name, merchant_name, payment_amount, payment_date)
where u.email = 'homerun@example.com';

insert into real_estate_properties (
  provider_id,
  property_name,
  address,
  region_code,
  district_code,
  base_price_amount,
  latitude,
  longitude,
  housing_type,
  contract_traps
)

insert into news_master (
  news_id,
  title,
  sentiment,
  source_name,
  article_text,
  economic_cycle_type,
  reason,
  sector_impact,
  exchange_rate_impact,
  real_estate_impact,
  job_impact
) values
('01500801.20200519071906001',
  '8월부터 아파트 분양권 전매제한 기간 확대…달아오른 청약열기, 한풀 꺾일까 계속될까',
  'negative',
  '영남일보',
  $news$오는 8월부터 예정된 분양권 전매제한 기간 확대를 피하기 위해 건설업체들이 7월까지 대거 분양에 나설 것으로 보인다. 마지막 분양권 프리미엄시장을 노리는 투자자들로 과열청약 열기가 계속될지, 안정된 분양시장을 원하는 실수요자의 관망심리로 열기가 수그러들지 관심이 쏠리고 있다. 정부는 최근 대구 등 광역시와 수도권의 분양권 전매제한 기간을 8월부터 기존 6개월에서 소유권 이전 등기 시까지로 강화했다. 그동안 수성구에만 해당되던 분양권 전매금지가 대구지역 8개 구·군으로 확대된다. 전매제한 기간 확대가 장기적으로 실수요자 시장 확대와 함께 공급물량 조정으로 이어질 것으로 예상되면서 건설업체들이 '8월 이전 분양'을 적극 검토하고 나섰다. 지역 건설업계에 따르면 그동안 8월 이후 분양을 내정한 서구 A단지는 정부 발표 이후 6월 분양으로 잠정 선회했다. 이밖에 5∼6개 단지 분양이 8월 전으로 앞당겨질 예정이다. 이런 가운데 최근 수년간 뜨거웠던 청약 열기가 지속될지를 놓고 의견이 엇갈리고 있다. 우선 수백대 1과 같은 기록적 청약경쟁률은 아니더라도 당분간 청약시장을 노리는 사람들이 적지 않을 것이라는 시각이 많다. 송원배 대구경북부동산분석학회 이사는 "분양권 전매가 가능하기 때문에 투자자가 몰릴 것으로 예상된다"며 "이를 노리는 건설업체들은 8월 분양권 전매제한 확대 이전인 6∼7월로 분양시기를 앞당기려고 할 것"이라고 내다봤다. 또 김대명 대구과학대 금융부동산과 교수는 "현금이 풍부한 투자자 입장에선 오히려 청약경쟁률이 낮아진 분양시장이 매력적 투자처가 될 수도 있다. 단, 입지에 따른 분양시장 양극화 현상은 빨라질 가능성이 높다"고 했다. 반면, 전매제한 기간 확대를 기점으로 대구 분양시장이 실수요자 위주의 청약시장 재편과 함께 안정세로 접어들 것이란 분석도 힘을 얻고 있다. 지역 부동산업계 한 관계자는 "투자수요 감소로 청약경쟁률이 하락하면 미분양이 대거 발생할 수 있어 굳이 수천만원의 프리미엄을 주고 아파트를 살 이유도 사라진다. 실수요자 가운데 일단 지켜보자는 분위기가 형성될 것으로 보인다"며 대구 분양시장이 조정기를 맞을 것으로 예상했다. 또 다른 건설업계 관계자는 "이제 분양권이 단기 투자처로 각광받는 시대는 저물고 있다. 8월 이후 투자수요의 급격한 감소가 예상돼 대구 부동산 시장에 투입된 자금이 급격히 이탈할 가능성도 배제할 수 없다"고 지적했다. 한편, 이와 별도로 대구지역 분양 물량 가운데 60% 이상을 차지하는 재건축·재개발 단지는 법적 요건 등 다양한 이유로 8월 이전 분양이 불가능하기 때문에 사업 추진에 제동이 걸릴 가능성이 높다.$news$,
  'BOOM_TO_CRISIS',
  '청약 열기와 분양권 프리미엄을 노린 투자수요가 과열돼 있다. 전매제한 강화 이후 투자수요 급감, 미분양 증가, 자금 이탈 가능성이 직접 제시돼 급랭 신호가 강하다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01200101.20200514195517001',
  '인천 부동산시장 외부매수세력 투기 증가…매매가·거래량 거품 드러나',
  'negative',
  '경기일보',
  $news$코로나19발 경기침체에도 폭등했던 인천지역 부동산시장에 서울 등의 매수 세력이 투기목적 등으로 유입했다는 인천시의 분석이 나왔다. 이들 외부 매수 세력으로 인천주택의 매매가는 정부의 12ㆍ16 부동산 대책 이후부터 3개월만에 평균 12.4%가 오른 것으로 나타났다. 13일 시에 따르면 지역에서 거주하는 시민의 인천주택 거래량은 2월 1만5천227건에서 3월 1만1천27건으로 27.6%가 감소했다. 반면, 서울 거주자의 인천주택 거래량은 같은 기간 1천800건에서 2천88건으로 16%가 늘어났다. 특히 서울 거주자의 인천주택 거래량은 2019년 12월 16일 정부의 12ㆍ16대책 발표 이후부터 증가세를 보이고 있다. 2020년 3월 서울 거주자의 인천주택 거래량과 2019년 12월 거래량(1천24건)을 비교하면 무려 103.9%가 증가했다. 또 서울을 제외한 다른 시ㆍ도 거주자의 인천주택 거래량은 2019년 12월 2천567건에서 2020년 3월 5천662건으로 121.6%가 늘어났다. 군ㆍ구별로는 청라국제도시가 있는 서구의 주택 거래량 증가가 두드러진다. 시민의 인천주택 거래량이 줄어드는 상황에서도 서구의 주택거래량은 2019년 12월에서 2020년 3월까지 165.3%가 증가했다. 더욱이 수도권광역급행철도(GTX-B)의 호재를 만난 남동구는 같은 기간 81.3%의 거래량 증가를 나타냈다. 이 같은 현상에 대해 시는 투기 목적 등을 가진 외부 매수 세력이 정부의 부동산 규제를 피해 비규제지역인 인천으로 몰려들었을 것이라고 분석 중이다. 이들 외부 매수 세력이 인천의 부동산시장으로 들어오면서 매매가 역시 천정부지로 치솟았다. 2017년 11월을 기준으로 인천주택의 매매가는 4.6%가 상승했다. 중간가격대를 의미하는 중위매매가격 역시 2019년 12월 2억1천71만2천원에서 2020년 3월 2억3천691만원으로 12.4%나 올랐다. 부동산시장 분위기에 예민한 송도국제도시 아파트들의 경우에는 1월과 3월 사이에 중위매매가격이 1억원 이상이나 폭등하는 현상까지 나타났다. 시 관계자는 “외부 매수 세력의 영향으로 인천의 주택 가격이 상승하는 현상을 파악하고 있다”며 “이러한 부분에 우리도 신경쓰고 있는 상태”라고 했다.$news$,
  'BOOM_TO_CRISIS',
  '외부 매수세 유입으로 거래량과 매매가가 급등하고 투기성 과열이 뚜렷하다. 기사 자체가 거품을 지적해 이후 조정·위축 위험을 강하게 시사한다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01500701.20210419195444001',
  '과열 우려 ‘가상화폐 거래’ 불법행위 특별단속',
  'negative',
  '부산일보',
  $news$정부가 가상화폐 거래가 국내 주식거래 규모를 뛰어 넘는 등 시장이 급성장하자 자금세탁·사기 등 불법행위에 대해 특별단속에 나섰다. 19일 국무조정실에 따르면 정부는 16일 가상자산 관계부처 차관회의를 열고 올 6월까지 금융위원회 경찰 공정위 등이 참여하는 범정부 차원의 특별단속을 진행하기로 했다. 불법 의심거래나 외국환거래법 위반 등이 있는지 살펴보고 가상화폐 거래소의 불공정 약관도 들여다본다는 것이다. 금융위는 가상자산 출금 때 은행이 1차 모니터링을 강화하도록 하고, 금융정보분석원(FIU)의 불법 의심거래 분석 결과가 경찰과 국세청에 신속히 통보되도록 수사 공조체계를 강화하기로 했다. 경찰은 가상자산 불법행위 유형별로 전담부서를 세분화하고 가상자산 추적 프로그램 보급을 늘리는 등 전문성 강화에 힘쓴다는 계획이다. 공정위는 가상자산 사업자의 불공정 약관을 찾아 시정할 계획이다. 기획재정부도 외국환거래법 등 불법 여부에 대한 점검을 강화한다. 또 국민들이 가상자산 사업자의 신고 진행 현황을 알 수 있도록 FIU 홈페이지에 현황을 공개할 예정이다. 구윤철 국조실장은 “가상자산의 가치는 누구도 담보할 수 없고 가상자산 거래는 투기성이 매우 높은 거래이므로 자기 책임하에 신중하게 판단해야 한다”고 말했다. 김덕준 기자$news$,
  'BOOM_TO_CRISIS',
  '가상화폐 거래가 급팽창하며 투기 과열 상태로 묘사된다. 동시에 범정부 특별단속과 감시 강화가 예고돼 단기 위축 가능성이 높다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01500701.20210107175243001',
  '부산 조정대상지역 지정 효과…아파트값 상승폭 축소',
  'mixed',
  '부산일보',
  $news$1월 첫째주 부산의 주간 아파트 매매가격이 전주보다 상승폭이 약간 축소됐다. 조정대상지역 지정 효과가 나타나는 모습이다. 7일 한국부동산원이 발표한 ‘1월 1주(1월 4일 기준) 주간 아파트 가격동향’에 따르면 부산은 0.45%, 울산은 0.48%가 올라 전국적으로 2위, 1위의 상승률을 기록했다. 부산은 전주에 0.58%가 오른 데 비해선 오름세가 약해졌다. 구·군별로 보면 전주에 1.35%가 올랐던 기장군이 0.75%로 상승률이 많이 떨어졌으나 부산에선 1위였다. 이어 강서구와 사상구가 나란히 0.60%, 사하구가 0.55%, 금정구 0.54%, 남구와 북구가 0.50%, 부산진구가 0.42%였다. 해운대는 0.37%로 상승률이 많이 하락했다. 한국부동산원은 “부산은 지난해 11월과 12월 두 차례 조정대상지역 지정 이후 상승폭이 축소된 가운데 기장군은 저평가 인식과 키 맞추기 영향 등으로, 강서구는 명지국제도시와 녹산산단 인근 신호동 위주로, 사상구는 주례·모라동 등 구축 대단지 위주로 상승했다”고 밝혔다. 주목할 부분은 경남 양산시다. 양산은 지난해 12월부터 크게 오르기 시작하면서 12월 마지막 주에는 1.07%가 올라 급등세를 나타냈는데 이번에는 0.64%가 올랐다. 상승폭이 축소됐지만 여전히 높은 편이다. 부산의 전세 가격 상승폭도 0.46→0.39%로 축소됐다. 구·군 중에서는 기장군이 0.89%로 가장 높았다. 이영래 부동산서베이 대표는 “겨울철 비수기에 조정대상지역 지정 효과가 나타난 것으로, 기장과 양산도 약간 주춤한 모습”이라며 “당분간 상승세는 유지되겠지만 상승폭은 줄어들 가능성이 높다. 조정대상지역으로 지정되면 2주택부터 취득세가 8% 붙는 것도 외지인 매수를 줄이는 데 많은 역할을 할 것”이라고 말했다.$news$,
  'BOOM_TO_RECOVERY',
  '부산과 양산이 전국 최상위 상승률을 기록할 정도로 현재는 과열 상태다. 다만 조정대상지역 지정 이후 상승폭 축소와 외지인 매수 둔화가 확인돼 다음 흐름은 급락보다는 점진적 안정화로 보는 것이 타당하다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01100201.20210219040745001',
  '설 이후 상승폭 줄어든 아파트값… 강남3구·양주 등 기세 꺾였다',
  'mixed',
  '국민일보',
  $news$대규모 공급 계획을 담은 2·4 부동산 대책이 발표된 지 열흘여 만에 전국 아파트 매매가와 전세가가 일제히 상승 폭을 줄여나가고 있다. 서울 강남 3구와 경기도 고양, 남양주, 양주 등 최근 집값 상승세를 이끌던 지역들이 진정세로 돌아섰다. 집값 열기가 식으면 인근 다른 지역 부동산으로 관심이 쏠리는 ‘풍선효과’도 나타나지 않았다. 2·4 대책의 윤곽이 여전히 뚜렷하지 않은 상황에서 대책 효과가 반영된 것으로 보기는 어렵다. 전문가들은 지난해 12월 매매가와 전세가가 급격히 치솟으면서 이미 가격 상승 피로감으로 조정기가 올 것으로 예상한 바 있다. 여기에 설 명절과 2·4 대책으로 인한 ‘주택 현금 청산’ 논란이 겹치며 거래절벽 상황으로 접어드는 것 아니냐는 해석이 나온다. 18일 한국부동산원이 발표한 2월 셋째 주(15일 기준) 주간 아파트 가격 동향 통계에 따르면 전국 아파트 매매가격 변동률은 0.25%로 1주일 전 0.27%에 비해 상승 폭이 다소 줄었다. 전주까지 3주 연속 역대 최고치인 0.33%를 기록했던 수도권도 0.30%로 일단 숨 고르기에 들어갔다. 서울 매매가 변동률이 0.08%로 전주(0.09%)보다 낮았고, 무서운 상승세를 보였던 경기도도 전주(0.46%)보다 0.04% 포인트 낮은 0.42%를 기록했다. 새해 들어 집값 상승세를 주도하던 지역 대부분에서 기세가 꺾였다. 서울에서는 강남구(0.09%)와 서초구(0.10%), 송파구(0.10%) 등의 상승 폭이 0.03~0.04% 포인트씩 감소했다. 경기도 남양주도 2월 첫째 주 매매가격 변동률이 0.96%까지 치솟으며 집값 상승을 이끌었지만 이번주엔 0.82%로 기세가 꺾였다. 한때 주간 변동률이 1%를 넘기며 올해 들어 전국에서 집값 상승률이 가장 높았던 양주 역시 0.82%였고, 최근 매매가격 변동률 1% 안팎을 유지하던 고양도 0.56%로 조정 양상이다. 거래량이 줄면서 가격 상승세도 정체된 것으로 보인다. 경기도부동산 포털에 따르면 지난달 경기도 아파트 거래량은 1만6077건으로 지난해 12월(2만3620건)에 비하면 감소세가 뚜렷하다. 신고 기한(30일)이 남았다는 점을 고려해도 연말 ‘패닉 바잉’의 기세는 일단 꺾인 셈이다. 집값 상승에 대한 피로감이 반영된 결과라는 분석이다. 또 때마침 설 연휴와 2·4 대책 영향으로 2월 거래량은 더 줄어들 가능성도 제기된다. 부동산원도 “(서울 집값은) 2·4 대책 발표 후 매수문의 감소와 관망세가 나타나는 가운데 명절 연휴 등의 영향으로 매수세가 위축되며 상승폭이 축소됐다”고 설명했다.$news$,
  'BOOM_TO_RECOVERY',
  '수도권이 직전까지 역대 최고치 상승률을 기록했고 강남3구와 양주 등 핵심 지역이 급등세를 주도해 현재는 명확한 과열 국면이다. 기사에서 상승폭 축소, 진정세, 거래량 감소가 함께 제시돼 단기적으로는 과열 완화와 숨 고르기 가능성이 높다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01601101.20210301161949003',
  '전주 아파트 가격 상승, 조정지역 지정이후 40분의1로 축소',
  'mixed',
  '전북일보',
  $news$전주지역이 부동산 조정지역 지정이후 아파트 가격 상승폭이 40분의 1수준으로 축소되고 전북 전체 아파트 가격상승도 미미한 수준에 그치며 안정세를 유지하고 있는 것으로 나타났다. 국토교통부는 지난해 12월 주거정책심의위원회 회의결과를 바탕으로 전주시를 부동산 조정대상지역으로 지정했다. 전주는 3개월간 해당 지역 주택가격상승률이 해당 지역이 포함된 시도 소비자물가상승률의 1.3배를 초과한 지역 중 △2개월간 청약경쟁률 5대1 초과 △주택보급률과 자가주택비율 전국 평균 이하 등 조정대상지정 기준에 모두 해당됐다. 전주시가 국토부의 부동산 조정대상지역에 포함된 것은 처음 있는 일로, 최근 전주지역 부동산 거래가 그만큼 비정상적 과열양상을 빚은 때문이다. 전주에서 부동산을 거래할 때 청약 1순위 자격요건이 청약통장 가입 후 6개월에서 2년으로 대폭 강화되고, 분양권 전매는 소유권이전등기 시점까지 금지된다. 주택담보와 전세대출 조건도 까다롭게 변경됐다. 전주시의 부동산 조정대상 지정 효과는 부동산 시장에 즉각적인 반응을 보였다. 지난 해 1월이후 매달 0.3~0.8%P씩 가파른 상승세를 기록하던 전주지역 아파트 가격이 0.1%대 상승에 그치고 있으며 전북지역 전체 아파트 가격도 상승폭이 큰 폭으로 축소됐다. 한국 부동산원이 집계한 주간 아파트 가격동향에 따르면 지난 2월 전북지역의 아파트 가격은 0.11% 상승했고 전달에도 0.14% 상승에 그쳐 부동산 규제지역 지정이전인 지난 해 11월 0.74% 상승과는 큰 차이를 보였다. 전주지역도 지난달 0.03% 상승에 그쳐 1.4%가 올랐던 지난 해 11월보다 상승폭이 40분의1 이상으로 축소된 것으로 나타났다. 전주의 부동산 규제지역 지정으로 풍선효과가 우려돼 왔던 익산지역 아파트 가격도 지난 달 말 하락세로 전환됐다. 전북지역 부동산 업계 관계자는 “전주지역 조정지역 지정이후 에코시티와 효천지구 등 신규 택지 아파트 가격은 여전히 상식이상의 높은 가격을 유지하고 있지만 호가만 높게 형성돼 있을 뿐 거래가 이뤄지지는 않고 있다”며 “올해 연말이후 하향 안정세로 전환될 가능성이 크다”고 전망했다.$news$,
  'BOOM_TO_RECOVERY',
  '전주 부동산이 비정상적 과열양상으로 규정될 만큼 강한 상승 국면이었지만, 조정대상지역 지정 이후 상승폭이 40분의 1 수준으로 급격히 축소됐다. 현재 과열의 잔열은 남아 있으나 단기 방향은 안정화와 하향 조정 쪽이 가장 뚜렷하다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200515162719001',
  '잘 나가는 셀트리온헬스케어, 1분기 매출 3000억 첫 돌파',
  'positive',
  '머니투데이',
  $news$셀트리온헬스케어는 올해 1분기 실적 발표를 통해 매출액 3569억원, 영업이익 558억원, 당기순이익 762억원을 기록했다고 15일 공시했다. 지난해 같은 기간과 비교하면 매출액 62%, 영업이익은 494% 증가한 수치다. 매출액 3000억원 돌파는 1분기 최초다. 글로벌 바이오제약 시장을 타겟으로 하는 사업 특성상 하반기로 갈수록 매출이 증가하는 구조를 보이는데 올해 1분기에는 전 제품의 고른 성장으로 지난해 4분기를 뛰어넘는 높은 성장률을 기록했다. 셀트리온헬스케어는 △램시마 △트룩시마 △허쥬마 등 주력 바이오시밀러 제품이 유럽에서 꾸준히 처방되고 있다. 인플렉트라(미국제품명 램시마), 트룩시마의 미국 판매가 확대된 것이 1분기 실적 개선의 주요 원인으로 분석된다. 실제로 의약품 가격이 높은 미국에서 지난해 말부터 판매가 시작된 트룩시마의 성장세가 뚜렷하다. 미국 헬스케어 정보서비스인 심포니헬스에 따르면 트룩시마는 지난해 11월 출시 후 5개월 만인 지난 3월 7.9%의 시장 점유율을 기록하며 미국 처방이 빠르게 늘었다. 셀트리온헬스케어는 “트룩시마의 판매 호조 속에 지난 3월 미국에 출시된 허쥬마가 본격 판매에 돌입했고, 인플렉트라 처방 역시 꾸준히 늘고 있는 만큼 매출 성장과 수익성 개선은 앞으로도 지속될 것”이라고 했다. 올해 런칭한 램시마SC가 유럽 현지에서 좋은 반응을 얻고 있는 점도 성장 기대감을 키운다. 램시마SC는 유일한 인플릭시맙 피하주사제형으로 정맥주사제형(IV)과 함께 처방해 치료 효과를 높일 수 있는 듀얼 포뮬레이션(Dual Formulation)의 강점을 갖고 있다. 특히 환자 스스로 집에서 투여할 수 있는 만큼 코로나19 상황에서 환자들의 감염 피해를 최소화 할 수 있는 효과적인 치료제로 주목받고 있다. 램시마SC가 출시된 독일, 영국, 네덜란드에서는 시장의 관심 속에 처방이 확대되고 있다. 하반기 램시마SC에 대한 IBD 적응증 추가가 완료될 경우 수요가 더욱 늘어날 전망이다. 셀트리온헬스케어는 “램시마SC에 대한 관심이 지속적인 처방으로 이어질 수 있도록 총력을 기울여 올해 말까지 유럽 전역으로 출시를 확대할 것”이라고 밝혔다.$news$,
  'BOOM_TO_BOOM',
  '1분기 매출 3000억 첫 돌파 및 영업이익 급증으로 실적 활황 국면이다. 미국·유럽 처방 확대와 하반기 수요 증가 전망으로 성장 지속을 시사한다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200508165307002',
  '와이아이케이, 1Q 영업익 35억원..전년比 ''흑전''(상보)',
  'positive',
  '머니투데이',
  $news$반도체 웨이퍼 테스터 검사장비 제조 및 판매 전문업체 와이아이케이는 1분기 연결기준 영업이익이 35억700만원으로 전년동기대비 흑자전환했다고 8일 공시했다. 매출액은 같은기간 159.9% 증가한 338억5400만원, 순이익은 37억3800만원으로 흑자전환했다. 와이아이케이 관계자는 "서버수요 증가 및 해외고객다변화가 호실적을 이끌었다"며 "자회사 샘씨엔에스가 역대 분기 최대 매출과 영업이익을 달성했다고 말했다. 회사는 코로나19 상황에서도 23분기 실적 성장을 자신하고 있다. 이 관계자는 "코로나19로 해외 납기 일정이 변경되어 일부 매출이 이월, 23분기 매출에 반영된다"며 "1분기 NAND 장비 매출 기여도가 컸다면 2분기부터 DRAM 장비 매출이 더해져 큰 폭의 실적 성장이 예상된다"고 말했다. 또 샘씨엔에스도 2분기 분기 최대 매출과 영업이익을 달성 할 것으로 예상하고 있다. 기존 NAND 와 DRAM시장 중심에서 비메모리용 STF개발을 통해 비메모리 영역까지 매출 확대가 기대된다. 이 관계자는 “현재 주력고객사가 NAND의 경우 하반기 기존라인의 보안투자 및 해외 신규투자 증량 검토 중에 있으며, DRAM 또한 신규라인에 증량을 검토하고 있다"며 "회사가 신규 개발중인 설비도 함께 검토되고 있다”고 덧붙였다.$news$,
  'BOOM_TO_BOOM',
  '서버 수요 증가로 매출 급증·흑자전환했고, DRAM 장비 매출 추가 및 투자 검토로 추가 성장 기대가 크다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200506094153001',
  '5년만에 20배↑…카카오페이지,일일 IP 거래액 20억원 돌파',
  'positive',
  '머니투데이',
  $news$카카오페이지는 업계 최초로 국내외에서 유통된 IP(지식재산) 통합 일 거래액이 20억원을 넘겼다고 6일 밝혔다. 카카오페이지에 따르면 국내와 일본 픽코마 등 해외에서 유통된 카카오페이지 IP 통합 일 거래액(1일 기준)이 20억원을 넘었다. 2015년 처음으로 일 거래액 1억원을 넘어선 지 5년 만이다. 분기 통합 거래액도 늘었다. 올해 1분기 기준 국내외 IP 통합 거래액은 1000억 원을 웃돌며 전분기 대비 16%, 전년 동기 대비 41%의 성장세를 보였다. 해외 IP 유통 거래액은 전분기 대비 53%, 전년 동기 대비로는 164% 상승하며 IP 비즈니스의 가치가 더욱 확대되고 있다고 회사 측은 설명했다. 올 초 카카오페이지의 글로벌 플랫폼인 인도네시아 서비스는 이용자 편의성을 크게 개선하며 안정적 성장 기반을 마련했다. 또 카카오재팬의 만화플랫폼인 픽코마는 2016년 론칭 이래 매년 2배 이상의 성장세를 보이며 지속 성장중이다. 특히 픽코마는 외형 및 내실을 탄탄히 다지며 지난해 4분기 영업이익이 흑자로 돌아섰으며 올해 연간 기준으로도 흑자가 예상된다. 픽코마의 성장 배경엔 카카오페이지의 강력한 ‘K-Story IP’가 있다. 픽코마 내에서 카카오페이지의 IP는 고작 1.3%에 불과하다. 적은 IP 점유율에도 불구하고 카카오페이지의 검증된 IP는 만화 종주국인 일본에서 픽코마의 성장을 견인하고 있다. 이진수 카카오페이지 대표는 “카카오페이지는 전세계 K-Story 선두주자로서의 사명감을 바탕으로 내실 있는 IP를 만들기 위한 밸류체인을 구축하는데 오랜 시간을 투자해왔다"며 "그 결과 카카오페이지만의 경쟁력 있는 K-Story IP를 갖추게 됐고 국내외 IP 통합 일 거래액 20억 원이라는 성과를 냈다”고 말했다. 카카오페이지는 일본 픽코마가 매우 가파른 성장세를 보이고 있는 만큼 일본 시장을 거점으로 올해 글로벌 진출에 더욱 박차를 가할 계획이다.$news$,
  'BOOM_TO_BOOM',
  'IP 거래액과 분기 거래액이 큰 폭으로 증가하고 해외 성장 및 흑자전환 전망이 제시된다. 고성장 지속 계획으로 확장 국면이 이어질 가능성이 높다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200511180956001',
  '세계 두번째로 오래된 항공사도 무릎 꿇었다',
  'negative',
  '머니투데이',
  $news$코로나19(COVID-19)로 인한 팬데믹(대유행)에 전세계에서 두 번째로 오랜 역사를 자랑하는 항공사도 파산 위기에 몰렸다. 11일 로이터, 파이낸셜타임스(FT) 등에 따르면 전세계에서 두 번째로 오래된 항공사로 여겨지는 콜롬비아의 '아비앙카(Avianca)'가 전일 채권 지급 기한을 맞추지 못해 챕터11(미국 파산법 11조)에 따른 파산을 신청했다. 이 회사는 중남미에서 두 번째로 큰 항공회사이며 콜롬비아 보고타에 본사를 두고 있다. 챕터 11은 미 연방파산법에 따른 파산보호 신청으로 우리나라 기업회생절차와 유사한 제도다. 미국내 법인이나 자산이 있는 기업이라면 외국에 본사를 둔 기업이라 할지라도 미국 법원에 챕터11을 신청할 수 있는 것으로 알려졌다. 안코 반 데어 베르프 아비앙카의 최고경영자(CEO)는 성명을 내고 "아비앙카는 100년의 역사 중 가장 도전적 위기에 직면해 있다"며 "이 프로세스(챕터11 신청)에 진입하는 것은 우리 재정적 문제 해결을 위해 필요한 단계"라고 밝혔다. FT에 따르면 아비앙카는 칠레의 '라탐 항공'에 이어 중남미에서 두 번째로 큰 항공사이고 또 네덜란드 KLM에 이어 세계에서 두 번째로 오래된 항공사다. 아비앙카는 1919년 설립됐다. 올해로 창립 101년째를 맞은 것이다. 로이터는 "아비앙카가 파산 상태에서 벗어나지 못한다면 이는 팬데믹의 결과로서 (파산한) 세계 최초 메이저 운송사 사례가 될 것"이라고 보도했다. 아비앙카는 남아메리카 국가들이 국경을 봉쇄한 여파로 지난 3월 말부터 정기 여객 운항을 중단한 상태였다. 아울러 2만 여 직원 대부분이 무급 상태로 위기를 지나고 있었다. 아비앙카는 콜롬비아 정부로부터 구제금융을 바랐지만 이는 불발된 것으로 전해졌다. 아비앙카는 지난 한 해 동안에만 미주 유럽 27개국 76개 행선지로 총 3000만 명이 넘는 승객들을 실어 날랐다. 지난해 매출 규모는 46억달러다. 3월 항공편이 결항된 이후 연결 수익은 80% 이상 감소했다. 2000년대 초에도 파산 위기에 놓인 적이 있었다. 포춘에 따르면 현재 이 회사의 부채 규모는 100억달러, 자산도 동일한 규모인 것으로 파악됐다.$news$,
  'CRISIS_TO_CRISIS',
  '코로나19로 항공편 중단, 수익 80% 급감, 무급휴직 및 파산보호 신청 등 업황 급락이 현재 진행형이다. 단기 내 회복 신호 없이 위기 지속을 시사한다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200506110634001',
  'SK이노도 무너졌다…현실화한 정유 4사 ''4兆 적자공포''',
  'negative',
  '머니투데이',
  $news$SK이노베이션이 1분기 1조7752억원 규모의 적자를 냈다. 창사 이래 최악의 실적으로 정유업계 전반에 불어닥친 유가 급락과 코로나19(COVID19) 확산에 따른 정제마진 하락에 따른 결과다. 정유 빅4(SK이노베이션, GS칼텍스, 에쓰오일, 현대오일뱅크)가 1분기에만 총 4조원 규모 적자를 볼 것이라는 전망이 사실상 현실화하는 분위기다. SK이노베이션은 6일 연결재무제표 기준 올해 1분기 매출액이 전년보다 12.6% 감소한 11조1630억원을 기록했다고 밝혔다. 영업손실은 1조7752억원으로 같은 기간 적자전환했다. 당기순손실은 1조5521억원으로 이 역시 적자전환했다. 유가급락으로 대규모 재고 관련 손실이 발생한데다, 코로나19발 국내외 석유제품 수요부진으로 인한 정제마진 약세까지 겹쳐 석유사업이 큰 폭의 적자를 기록했기 때문이다. 지난해부터 심각해진 시장상황 악화 속에서 코로나19 영향과 국제유가 급락 등 소위 3중고 영향이다. 유가 급락으로 인한 재고관련 손실 규모는 9418억원, 항공유와 휘발유 등 상품 가격이 원유가격보다 낮아지는 역마진 등으로 석유사업에서만 1조 6360억원의 적자를 기록했다. 매출 또한 유가하락으로 인한 석유제품 판매단가 하락과 수요 위축에 따른 판매 물량 감소로 분기 매출 기준으로 2017년 2분기 10조 5413억원 이후 가장 낮은 수치를 기록했다. 화학사업에서는 전분기보다 제품 마진이 개선되었음에도 불구하고 납사 가격 하락에 따른 재고 손실 영향으로 영업이익이 전 분기보다 971억원 줄어들어 898억원 적자를 기록했다. 화학사업의 분기 적자는 2015년 4분기 이후 처음이다. 김준 SK이노베이션 총괄 사장은 "코로나19 영향으로 사상 최악의 경영환경에 놓여 있지만, 사업 체질을 개선하고 비즈니스 모델을 혁신하는 기회로 삼아 위기를 극복해 가고 있다"고 말했다. SK이노베이션의 대규모 적자에 따라 정유 4개사의 1분기 합산 영업손실이 4조원 수준이 될 수 있다는 공포가 현실화했다. 앞서 실적을 발표한 에쓰오일과 현대오일뱅크는 각각 1조73억원, 5632억원의 영업손실을 냈다. 이 역시 창사 이래 분기기준 최악 실적이었다. 업계에서는 GS칼텍스의 영업손실이 7000억원을 넘어설 수 있다는 전망이 나오는데, 이 경우 4사 합산 손실 규모는 4조원을 넘어서게 된다. 업계 적자공포는 1분기에 그치지 않을 수 있다는 우려도 고개를 든다. 전 세계적 코로나19 확산세가 언제 진정될 지 알 수 없는 데다, 수요 둔화에 따른 마진 위축도 여전하기 때문이다. 이 같은 상황이 상당 기간 장기화하면, 정부의 업계 지원책 '약발'도 먹히지 않는다는 것이 업계 시각이다.$news$,
  'CRISIS_TO_CRISIS',
  '유가 급락·수요 위축으로 정유 4사 대규모 적자와 마진 붕괴가 현실화됐고, 장기화 우려가 커 위기 국면 지속 신호다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01500801.20200502071800001',
  '99개월 만에 무역 적자로',
  'negative',
  '영남일보',
  $news$코로나19 확산 여파로 98개월간 이어지던 무역수지 흑자행진이 중단됐다. 미국과 일본·유럽 등 우리의 주요 수출 대상국이 코로나 팬데믹(세계적 대유행)으로 수요가 감소한데 따른 것으로 분석된다. 산업통상자원부는 1일 지난달 우리나라 수출은 369억2천만달러로, 지난해 4월보다 24.3% 감소했다고 밝혔다. 하루 평균 수출도 16억7천800만달러로 같은 기간 일평균보다 17.4% 줄었다. 수입은 15.9% 감소한 378억7천만달러를 기록하면서 무역수지는 9억5천만달러 적자로 나타났다. 이로써 무역수지는 2012년 1월 이후 99개월 만에 적자로 돌아섰다. 주요 수출품목 중 우리의 주력 산업인 17개 품목이 마이너스를 나타냈다. 자동차부품(-49.6%), 자동차(-36.3%), 디스플레이(-39.1%), 반도체(-14.9%), 일반기계(-20.0%), 석유화학(-33.6%), 철강(-24.1%) 등이 감소했으며 특히 석유제품(-56.8%)과 선박(-60.9%)의 감소세가 두드러졌다. 정부는 4월 무역적자가 '일시적·불가피한 현상'이라고 평가했다. 글로벌 금융위기로 무역수지 적자가 발생한 2009년 1월과 비교했을 때 당시엔 수출과 수입이 모두 감소한 '불황형 적자'를 기록한 반면 지난달엔 국내 생산에 기여하는 중간재·소비재 수입의 감소폭이 적고 수입 구조도 양호하다는 것. 또 조업일수 부족·역기저효과가 수출 감소폭을 키운 것도 원인이라고 덧붙였다. 반면 코로나19 확산으로 △비대면 산업 △홈코노미(Home+Economy) △K방역 산업 관련 품목의 수출은 크게 늘었다.$news$,
  'CRISIS_TO_CRISIS',
  '수출 -24.3% 급감과 99개월 만의 무역수지 적자 전환은 대외수요 붕괴에 따른 경기 위축을 강하게 시사한다. 일부 품목 증가가 있으나 전반적 충격이 더 크다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200520080147001',
  '성윤모 "섬유패션산업 특별고용지원 업종 지정 적극 검토"',
  'mixed',
  '머니투데이',
  $news$성윤모 산업통상자원부 장관이 20일 "섬유패션업계가 요청한 저금리 긴급경영안정자금 지원, 특별고용지원업종 지정 등을 범정부적으로 적극 검토할 것”이라고 말했다. 성 장관은 이날 서울 중구 대한상공회의소에서 열린 '제3차 산업·기업 위기대응반 회의 및 제5차 포스트 코로나 산업전략 대화'에서 "일시적 유동성 위기로 기업이 쓰러지는 일이 없도록 기업이 직면한 경영상 애로 해소를 적극 돕겠다"며 이같이 밝혔다. 이날 간담회에서 섬유패션업계는 그동안 유동성 위기 해소를 위해 특별고용지원업종으로 지정해 줄 것을 요청했다. 전날 한국섬유산업연합회가 지정 신청서를 내기도 했다. 특별고용지원업종은 고용 사정이 급격히 악화할 우려가 있는 업종에 대해 사업주와 근로자에게 각종 지원을 해주는 제도다. 또 정부조달 조기집행, 대규모 소비활성화 행사 조기 개최 등을 통해 내수를 진작하고, 온라인 플랫폼 인프라 구축, 퇴직자 등을 활용한 디지털 수출전문가 양성 지원도 요청했다. 이러한 요청은 최근 섬유패션산업이 코로나19로 직격탄을 맞았기 때문이다. 경기에 민감하고 대면 소비가 많다 보니 피해가 컸다. 산업부에 따르면 북미·유럽 패션 기업 80% 이상이 매장을 폐쇄했고 글로벌 패션의류기업의 평균 시가 총액도 올해 1분기 40% 가까이 감소한 것으로 추정된다. 국내 섬유패션업계도 글로벌 바이어들의 주문 취소와 신규 주문 급감, 대금 결제 지연으로 어려움을 겪고 있다. 지난달 섬유패션 수출도 전년동월대비 35.3% 급감했다. 정부는 당장 패션의류 소비를 늘리기 위해 다음 달 26일부터 7월10일까지 섬유센터에서 열리는 '대한민국 동행 세일' 기간에 '코리아 패션 마켓'을 열 계획이다. 50여개 패션 기업이 참여한다. 국방·공공 분야의 조기 발주 및 선대금 지급 등을 확대한다. 기업활력법 등을 활용해 저부가가치·범용 제품에서 고부가가치·산업용 제품 중심으로 섬유패션업계의 사업전환 및 업종재편도 적극 지원한다. 섬유패션업계도 자구노력에 나선다. 코로나19 이후 강화될 국내 생산 확대에 선제적 대응을 위해 국내 섬유패션 수요-공급기업간(원사-원단-봉제-패션업계) 상생협력을 통해 완제품 생산을 확대하는 '천리(千里) 프로젝트'를 추진한다. '천리'는 대략적인 우리나라 전역의 직선거리로 국내 생산을 의미한다. 삼성물산(패션)과 협력사 울랜드(직물)·씨에프씨(봉제)는 이날 회의직후 ‘천리 프로젝트’ 추진을 위한 상생협력 협약을 체결했다. K2코리아(패션)와 조광아이엔씨(직물), 동미산업(봉제), 삼덕통상(봉제)도 관련 협약식을 가졌다. 정부는 국내 소재산업의 경쟁력 강화 및 스마트 봉제공장 등 정책적 지원을 통해 ‘천리 프로젝트’와 같은 협력사례 확산을 지원키로 했다. 성 장관은 "코로나19로 우리 산업은 전과 다른 양상으로 전개될 것"이라며 "현재의 위기를 극복하고 경쟁력 강화를 위해 함께 노력한다면 섬유패션산업이 다시 한번 전세계를 선도할 수 있을 것"이라고 말했다.$news$,
  'CRISIS_TO_RECOVERY',
  '코로나19로 주문 취소·신규주문 급감, 수출 35.3% 급감 등 업황이 위기 국면이다. 특별고용지원·긴급자금·소비활성화 행사 등 정책 지원과 사업전환 추진이 단기 완화/회복 신호다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200511085844001',
  '만도, 2Q 이후 하반기 수익 개선 예상-유안타',
  'mixed',
  '머니투데이',
  $news$유안타증권은 만도에 대해 2분기 이후 하반기 수익 개선이 진행할 것이라고 예상했다. 투자의견은 '매수', 목표주가는 3만4500원을 유지했다. 8일 종가는 2만4700원이다. 남정미 유안타증권 연구원은 "만도의 1분기 매출은 전년 동기 대비 7.4% 줄어든 1조3101억원, 영업이익은 42.3% 감소한 185억원으로 시장 전망치(컨센서스)를 상회했다"며 "2분기까지 코로나 영향에 따른 매출 타격과 이에 따른 실적 악화는 불가피하다"고 예상했다. 남 연구원은 "5월부터 코로나19 이후 회복에 대한 시그널에 집중할 필요가 있다"며 "매출비중이 높은 한국, 중국 지역이 코로나영향에서 가장 빠르게 회복되며 동사 주가도 탄력을 받을 것"이라고 전망했다.$news$,
  'CRISIS_TO_RECOVERY',
  '2분기까지 코로나로 실적 악화가 불가피하나 5월 이후 회복 시그널과 하반기 수익 개선 전망이 있다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200506120207002',
  '''중소·소상공인 판로 지원''..4개 지자체-700개 업체 연계행사',
  'mixed',
  '머니투데이',
  $news$중소벤처기업부는 내수 활력 제고를 위한 '대한민국 동행세일' 개최지역 선정과 지원업체 모집 공모를 7일부터 시작한다고 6일 밝혔다. '대한민국 동행세일'은 코로나19로 침체된 소비심리를 되살리고 중소·소상공인의 판로 지원을 위해 올해 추경예산에 반영돼 진행되는 사업이다. 주요 내용은 △지역별 순회 현장행사 △온·오프라인 특별판매 기획전 △코로나 위기 극복 내수 활성화 캠페인 등이다. 먼저 17개 광역시·도를 대상으로 행사 제안서를 접수 받아 민간 추진자문단 자문을 거쳐 최종 선정심의위원회에서 총 4개 지자체를 선정한다. 코로나19 피해현황과 지역경제 활성화, 행사 유치 계획의 구체성과 실현가능성 등에 중점을 둬 평가한다. 중기부는 최종 선정된 지자체와 협의를 통해 지역 행사와 연계해 중소기업·소상공인 제품 특별 판매전, 다양한 먹거리·볼거리 행사 등 페스티벌형 야외 행사를 기획한다. 또 중소·소상공인을 대상으로 신청·접수를 받아 700개 내외의 업체를 선정한다. 신청업체에 대해서는 제품 평가와 경영 평가를 실시한다. 경영 평가에는 코로나19로 인한 매출 감소 등 피해 정도를 반영한다. 선정기업은 지역별 행사와 연계해 판매부스 설치 및 먹거리 행사 참여 등을 지원한다. 우수제품에 대해서는 가치삽시다 TV(라이브커머스 등), 홈쇼핑사 현장 판매 방송 등을 진행할 계획이다. 가치삽시다 플랫폼, 민간 온라인몰 등 온라인 플랫폼 사전 연계를 통해 특가세일, 할인 쿠폰 발행 등을 통해 제품 홍보·판매도 지원한다. 박영선 중기부 장관은 "코로나 19를 극복할 수 있는 가장 강한 힘은 상생과 공존"이라며 "대한민국 동행세일은 우리 경제의 내수활력 제고를 위한 전환점이 될 것"이라고 말했다. 이번 모집 공모의 신청기간은 오는 7일부터 21일까지다. 지자체는 공문을 통해 신청·접수를 받고, 참여기업은 아임스타즈 홈페이지에서 신청·접수를 받는다.$news$,
  'CRISIS_TO_RECOVERY',
  '코로나로 침체된 소비심리 회복과 내수 활성화를 목표로 대규모 판로·세일 행사를 추진해 경기 반등을 유도하는 정책 신호다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200512091221001',
  '현대리바트, 빌트인 가구 덕에 好好…주가 ↑',
  'positive',
  '머니투데이',
  $news$현대리바트가 호실적에 강세다. 12일 오전 9시10분 현대리바트는 전일대비 1900원(17.27%) 뛴 1만2900원을 나타내고 있다. 현대리바트는 1분기 연결기준 매출액은 전년 동기 대비 18.7% 증가한 3694억원, 영업이익은 전년 동기 대비 50.4% 늘어난 146억원을 기록했다. 영업이익이 100억원대를 넘어선 것은 6개 분기 만에 처음이어서 앞으로 실적 회복세가 가팔라질 것이라는 기대감이 커진다. 현대리바트는 과거 기업 간 거래(B2B) 중심에서 최근에는 맞춤형 가구 등 소비자 간 거래(B2C)에도 집중하고 있다. 남성현 한화투자증권 연구원은 "1분기 긍정적 추세는 2분기에도 이어질 것"이라며 "사무용가구의 경우 범 현대 물량이 일시적으로 증가할 가능성이 있고 빌트인 가구 수익성 개선에 따른 이익 기여도 증가 등으로 영업이익 증가세가 지속될 것"이라고 내다봤다.$news$,
  'RECOVERY_TO_BOOM',
  '매출·영업이익이 큰 폭 증가하고 2분기에도 긍정적 추세 지속 전망으로 실적 모멘텀이 강화됐다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200506100921001',
  '카뱅 1분기 순이익 전년比 181%↑…수수료 손실 줄어',
  'mixed',
  '머니투데이',
  $news$카카오뱅크가 이자이익 증가, 비이자손익 개선에 힘입어 1분기 전년보다 181% 늘어난 순이익을 거뒀다. 카카오뱅크(한국카카오은행)는 1분기 순이익이 185억원으로 지난해 같은 기간보다 181.3% 증가했다고 6일 밝혔다. 순이자수익은 545억원 늘어난 844억원, 순수수료손실은 148억원 개선된 31억원을 각각 기록했다. 제휴사 대출 추천, 주식계좌개설 신청 서비스가 수수료 손실폭을 메웠다. 최근 신용카드 발급을 시작해 수수료 개선세가 이어질 전망이다. 수익성은 다소 하락해 순이자마진(NIM)은 1.54%를 기록했다. 1분기 기준 연체율은 0.20%, 국제결제은행(BIS) 자기자본비율은 14.29%였다. 3월 말 기준 총자산은 23조4000억원 수준이었다. 지난해 같은기간보다 43.6% 늘었다.$news$,
  'RECOVERY_TO_RECOVERY',
  '순이익 급증과 수수료 손실 개선은 금융업 실적의 정상화 신호다. 다만 NIM 하락 등 혼재 요인이 있어 강한 확장(호황)으로 보긴 어렵다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('01601101.20210218195421001',
  '전북 군산형 일자리 ‘청신호’… 민간 투자유치 성공, 업무협약 등 잇따라',
  'positive',
  '전북일보',
  $news$산업부 공모가 진행 중인 전북 군산형 일자리에 대한 성공 기대감이 높아지고 있다. 앵커 기업인 ㈜명신에 민간 투자유치 성공 소식이 전해져왔고, 최근 한국자산관리공사와 전북도가 상생형 일자리 참여기업 지원 및 지역사회 발전을 위한 업무협약 체결하면서다. 실제 전북 군산형 일자리는 단순 지정에 그치는 것이 아닌 진정한 성공을 위해서는 지정 이후 원활한 사업 추진이 필수적이다. 특히, 전기차 생산 대수와 인력 고용 규모 등에 있어 앵커 기업으로 꼽히는 명신의 성공 여부가 주요하다. 최근 군산형 일자리와 관련해 잇따른 긍정적인 호재가 생기면서 이번 달 말 예정된 지정에 더해 안정적인 추진도 기대되는 시점이다. 18일 전북도 등에 따르면 최근 ㈜명신이 한국자산관리공사(캠코) 기업구조혁신지원센터를 통해 ‘한국투자프라이빗에쿼티㈜’로부터 550억 원 규모의 투자유치에 성공했다. 상생형 일자리 참여기업에는 현재 ㈜명신을 필두로, ㈜에디슨모터스, ㈜대창모터스, ㈜엠피에스코리아 등 전기자동차 완성차 업체 4개사와, ㈜코스텍 부품업체 1개사가 참여한다. 이 중에서 우선 ㈜명신이 캠코 기업구조혁신지원센터를 통해 한국투자프라이빗에쿼티㈜로부터 550억 원 규모의 민간자본 투자유치에 성공하면서 자금공급 등 기업경영에 활기를 띨 것으로 기대된다. 나아가 일자리 창출 등 지역경제 활성화에도 이바지할 것으로 전망된다. 아울러 이날 한국자산관리공사와 전북도가 전북도청에서 ‘전북 상생형 일자리 참여기업 지원 및 지역사회 발전을 위한 업무협약’을 체결했다. 이번 업무협약은 전북 도내 기업들이 캠코의 기업구조혁신지원센터를 통해 민간자본 투자를 유치해 기업의 경영 정상화와 지역경제 활성화, 일자리 창출 등을 도모하기 위해 추진됐다. 전북도는 캠코의 기업 지원과 사회공헌 활동을 행정적으로 지원하고, 캠코는 전라북도 상생형 일자리 참여기업의 민간 자본투자 연계를 지원하기로 하면서 도와 캠코, 기업 등 지역사회 전반이 함께 공동 발전할 수 있는 계기가 될 것으로 전망된다. 또한, 민간 투자자가 투자 타당성이 있다고 판단하는 경우 캠코의 기업구조혁신지원센터를 통해 상생형 일자리 참여기업뿐만 아니라 그 외의 기업에도 민간 투자를 연계할 계획이다. 송하진 도지사는 “이번 업무협약은 전북의 일자리 참여기업이 민간 자본투자를 유치하는 데 큰 도움이 될 것이라는 점에서 의의가 있다”며 “기업이 더욱 성장할 수 있는 확고한 기반이 다져질 것으로 기대되고, 도와 캠코, 기업 등 지역사회 전반의 상생과 발전을 위해 행정적 지원을 아끼지 않겠다”고 밝혔다.$news$,
  'CRISIS_TO_BOOM',
  '산업위기 지역이던 군산에 전기차 중심 상생형 일자리와 대규모 민간투자 유치가 가시화되며 지역경제가 반등을 넘어 성장 국면으로 진입할 가능성을 시사한다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200514084331001',
  '이마트, 2분기가 더 문제…재난지원금 간접피해 예상',
  'negative',
  '머니투데이',
  $news$NH투자증권은 14일 이마트에 대해 바닥은 통과했으나 여름이 보릿고개라며 투자의견 목표주가를 기존 15만원에서 13만원으로 하향했다. 투자의견은 'HOLD(보유)'로 유지했다. 1분기 이마트는 연결기준 매출 5조2108억원(14% y-y), 영업이익 484억원을 기록해 전년 동기 대비 각각 14% 늘고 35% 감소했다. 마트 본업은 매출 3조4660억원, 영업이익 854억원으로 전년 대비 각각 4% 늘고, 20% 줄었다. 이와 관련 이지영 연구원은 "코로나19로 인한 식료품 매출 호조로 기존점 성장률은 이마트 -2.4%, 트레이더스 7.1%로 양호했다"면서도 "그러나 비식품 매출이 부진해 전사 수익성도 하락했고, 비효율 전문점 폐점에 따른 일회성 비용도 50억원 반영됐다"고 분석했다. 2분기에는 더욱 실적이 악화될 것이라는 전망이다. 이 연구원은 "2분기 전통적 비수기에 정부의 긴급재난지원금 정책에 따른 부정적 영향이 예상된다"며 "대형마트의 핵심 카테고리인 식료품은 가구 지출에서 가장 큰 비중을 차지하는데, 오프라인 소상공인 사업자의 대부분이 외식 및 도소매업에 종사해 대체재 관계이기 때문"이라고 분석했다.$news$,
  'RECOVERY_TO_CRISIS',
  '1분기 바닥 통과 언급에도 2분기 비수기와 재난지원금 영향으로 실적 악화가 예상돼 단기 둔화 신호가 강하다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200512092150001',
  '''조기 폐점 공포'' 백화점, 이번엔 고객 아닌 직원이 확진',
  'negative',
  '머니투데이',
  $news$이태원 클럽 발(發) 코로나19(COVID-19) 확진자가 전국에서 동시다발적으로 나오면서, 유통가에 또다시 '도미노 임시폐점'이 잇따르고 있다. 올 1분기 코로나 초기 확산 당시와는 달리, 고객보다 대부분 젊은 내부 직원들이 확진 판정을 받은 게 차이점이다. 이달 '생활 속 거리두기' 조치 이후 매출 회복세를 기대했던 유통 업체들의 낙담이 커지고 있다. 12일 유통업계에 따르면 코스트코 양재점은 보건 당국으로부터 코로나 확진자 방문 기록 통보를 받아, 전날 오후 8시 조기 폐점을 했다. 이날 오전 8시부터는 정상 영업에 들어갔지만 매출 손실이 불가피 하다. 현대백화점은 충청점(충북 청주)과 중동점(경기 부천) 두 점포에서 잇따라 근무 직원 확진으로 임시 영업 중단을 해야했다. 충청점에서 근무하던 판매 사원이 코로나19 확진 판정을 받아 지난 9일 영업을 중단했다. 이 사원은 지난 6일부터 8일까지 사흘간 백화점 1층으로 출근한 것으로 파악됐다. 이어 중동점에선 고객이 많이 몰리는 일요인인 지난 10일 근무 직원의 확진자 통보로 조기 영업 종료를 했다. 이밖에 롯데백화점의 최대 매출 점포인 서울 소공동 본점에서 명품매장 판매사원이 확진 판정을 받아 지난 9일 오후부터 임시 휴점했다. 이밖에 패션업체 한세실업 역시 확진 판정자가 나오면서 지난 7일부터 서울 영등포구 회사 건물을 폐쇄하고, 전 직원에 재택 근무를 지시했다. 유통업계 관계자는 "코로나 사태로 올 1분기 오프라인 점포 실적이 매우 악화됐는데 그나마 이달 '생활 속 거리두기'로 전환하며 개선되나 했지만, 이태원발 코로나 확산이 찬물을 끼얹었다"며 "상황이 나아지길 기대할 뿐"이라고 했다.$news$,
  'RECOVERY_TO_CRISIS',
  '거리두기 완화로 매출 회복 기대가 있었으나 확진자 발생으로 임시폐점이 재개돼 오프라인 유통 회복이 다시 꺾일 가능성이 크다.',
  NULL,
  NULL,
  NULL,
  NULL
),
('02100201.20200514094704002',
  '넷마블 약세..실적 부진 영향',
  'negative',
  '머니투데이',
  $news$넷마블이 약세다. 올해 1분기 실적이 기대에 미치지 못 했다는 평가 등에 영향을 받은 것으로 해석된다. 14일 증시에서 넷마블은 오전 9시45분 현재 전일 대비 7700원(7.40%) 내린 9만6300원에 거래 중이다. 지난 13일 넷마블은 올해 1분기 매출액이 5329억원으로 전년 동기 대비 11.6% 증가하고, 영업이익은 204억원으로 전년 동기 대비 39.8% 감소했다고 공시했다. 오동환 삼성증권 연구원은 "넷마블 올해 1분기 영업이익은 컨센서스를 58.6% 하회하며 또 어닝 쇼크를 기록했다"며 "기존 게임들의 매출 감소와 신작 출시 지연, 예상을 상회하는 비용 증가세를 감안해 올해 영업이익 추정치를 1890억원으로 32.3% 하향조정 한다"고 분석했다.$news$,
  'RECOVERY_TO_CRISIS',
  '매출은 증가했지만 영업이익이 큰 폭 감소하고 어닝쇼크·비용 증가로 이익 전망이 하향돼 둔화 신호가 강하다.',
  NULL,
  NULL,
  NULL,
  NULL
)
on conflict (news_id) do update set
  title = excluded.title,
  sentiment = excluded.sentiment,
  source_name = excluded.source_name,
  article_text = excluded.article_text,
  economic_cycle_type = excluded.economic_cycle_type,
  reason = excluded.reason;


with registry_document_samples(registry_section, issue_summary, quiz_sample_payload) as (
  values
    ('GAPGU', '현재 소유권이전 이후 가압류가 말소되지 않고 남아 있어 매수인이 바로 주의해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2021년 3월 15일","reason":"보존","details":"소유자 주식회사 청명하우징"},{"rank_no":"2","purpose":"소유권이전","receipt":"2024년 10월 21일","reason":"2024년 10월 3일 매매","details":"소유자 김서준"},{"rank_no":"3","purpose":"가압류","receipt":"2025년 2월 7일","reason":"가압류결정","details":"청구금액 {claim_amount} 가압류권자 주식회사 한빛자산관리 서울중앙지방법원의 가압류결정","rendering":{"claim_amount":{"source":"sale_price_ratio","ratio_percent":12,"rounding_unit":"만원"}}}],"issueSummary":"현재 소유권이전 이후 가압류가 말소되지 않고 남아 있어 매수인이 바로 주의해야 하는 사례입니다.","keyPoints":["현재 소유자 앞으로 소유권이전이 완료된 뒤 가압류가 추가로 기재되어 있습니다.","갑구만 보더라도 현재 권리관계가 깨끗하지 않은 상태로 읽힙니다.","가압류는 매수 전 말소 여부를 반드시 확인해야 하는 대표 신호입니다."],"feedbackCorrect":"맞았습니다. 이 사례는 현재 가압류가 살아 있어 위험으로 보는 것이 맞습니다. 매수 전 말소 여부와 거래 진행 가능 여부를 반드시 확인해야 합니다.","feedbackWrong":"이 사례는 정상으로 보기 어렵습니다. 현재 소유권이전 이후 가압류가 남아 있으므로, 갑구만 봐도 매수인이 주의해야 하는 위험 사례입니다."}$$::jsonb),
    ('GAPGU', '현재 소유자 명의 뒤에 소유권이전등기청구권가처분이 남아 있어 소유권 분쟁 가능성을 의심해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2020년 8월 26일","reason":"보존","details":"소유자 이지안"},{"rank_no":"2","purpose":"소유권이전","receipt":"2024년 12월 4일","reason":"2024년 11월 20일 매매","details":"소유자 박시우"},{"rank_no":"3","purpose":"소유권이전등기청구권가처분","receipt":"2025년 1월 17일","reason":"가처분결정","details":"가처분권자 정유진 서울서부지방법원의 가처분결정"}],"issueSummary":"현재 소유자 명의 뒤에 소유권이전등기청구권가처분이 남아 있어 소유권 분쟁 가능성을 의심해야 하는 사례입니다.","keyPoints":["현재 소유권이전 이후 바로 분쟁성 보전처분이 기재되어 있습니다.","소유권이전등기청구권가처분은 매수인 입장에서 가장 직접적인 경고 신호 중 하나입니다.","갑구만 보더라도 거래를 서두르기보다 권리관계를 먼저 확인해야 합니다."],"feedbackCorrect":"맞았습니다. 소유권이전등기청구권가처분이 현재 남아 있는 경우는 소유권 분쟁 위험을 먼저 의심해야 합니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 소유권이전등기청구권가처분이 남아 있다는 점만으로도 매수인은 신중해야 합니다."}$$::jsonb),
    ('GAPGU', '짧은 기간 안에 소유권이전이 연속해서 반복되어 거래 흐름이 비정상적으로 빠르게 보이는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2019년 6월 20일","reason":"보존","details":"소유자 장민호"},{"rank_no":"2","purpose":"소유권이전","receipt":"2025년 1월 6일","reason":"2024년 12월 28일 매매","details":"소유자 김현우"},{"rank_no":"3","purpose":"소유권이전","receipt":"2025년 1월 24일","reason":"2025년 1월 10일 매매","details":"소유자 이서진"},{"rank_no":"4","purpose":"소유권이전","receipt":"2025년 2월 11일","reason":"2025년 2월 3일 매매","details":"소유자 박도윤"}],"issueSummary":"짧은 기간 안에 소유권이전이 연속해서 반복되어 거래 흐름이 비정상적으로 빠르게 보이는 사례입니다.","keyPoints":["한 달 남짓한 기간에 소유권이전이 여러 차례 이어졌습니다.","현재 소유자의 보유 기간이 매우 짧아 보입니다.","갑구만 보고도 거래 패턴 자체가 이상 신호로 읽히는 구조입니다."],"feedbackCorrect":"맞았습니다. 반복 이전 자체가 곧바로 불법을 뜻하는 것은 아니지만, 이렇게 짧은 기간에 연속 거래가 이어지면 매수인은 충분히 경계해야 합니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 짧은 기간 반복된 소유권이전은 갑구만으로도 주의 신호로 읽힙니다."}$$::jsonb),
    ('GAPGU', '중간 소유권이전등기가 말소되었는데 그 뒤의 소유권이전등기가 남아 있어 권리 연결 관계를 특히 조심해서 확인해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2018년 11월 2일","reason":"보존","details":"소유자 주식회사 다온개발"},{"rank_no":"2","purpose":"소유권이전","receipt":"2024년 7월 12일","reason":"2024년 7월 2일 매매","details":"소유자 김태훈"},{"rank_no":"3","purpose":"소유권이전","receipt":"2024년 9월 5일","reason":"2024년 8월 29일 매매","details":"소유자 이수빈"},{"rank_no":"4","purpose":"3번소유권이전등기말소","receipt":"2024년 11월 18일","reason":"해제","details":"2024년 9월 5일 접수 소유권이전등기 말소"},{"rank_no":"5","purpose":"소유권이전","receipt":"2024년 12월 6일","reason":"2024년 11월 25일 매매","details":"소유자 최도윤"}],"issueSummary":"중간 소유권이전등기가 말소되었는데 그 뒤의 소유권이전등기가 남아 있어 권리 연결 관계를 특히 조심해서 확인해야 하는 사례입니다.","keyPoints":["중간 순위의 소유권이전등기가 후순위에서 말소되었습니다.","그 뒤 후속 소유권이전등기가 그대로 남아 있어 흐름이 단순하지 않습니다.","갑구만 보더라도 권리 연결이 매끄럽지 않아 추가 검토가 필요한 구조입니다."],"feedbackCorrect":"맞았습니다. 중간 소유권이전등기 말소 후 후속등기가 남아 있는 경우는 권리관계를 더 깊게 확인해야 하므로 위험 신호로 보는 편이 맞습니다.","feedbackWrong":"이 사례는 정상으로 보기 어렵습니다. 중간 등기 말소와 후속 소유권이전이 함께 보이면 권리 연결 관계를 면밀히 살펴봐야 합니다."}$$::jsonb),
    ('GAPGU', '가압류가 반복되어 등장한 데 이어 소유권이전등기청구권가처분까지 남아 있어 분쟁 흔적이 누적된 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2020년 5월 14일","reason":"보존","details":"소유자 유한회사 다온리빙"},{"rank_no":"2","purpose":"소유권이전","receipt":"2024년 8월 8일","reason":"2024년 7월 29일 매매","details":"소유자 정하린"},{"rank_no":"3","purpose":"가압류","receipt":"2024년 10월 2일","reason":"가압류결정","details":"청구금액 {claim_amount_primary} 가압류권자 주식회사 미래채권관리 인천지방법원의 가압류결정","rendering":{"claim_amount_primary":{"source":"sale_price_ratio","ratio_percent":12,"rounding_unit":"만원"}}},{"rank_no":"4","purpose":"3번가압류등기말소","receipt":"2024년 11월 7일","reason":"해제","details":"2024년 10월 2일 접수 가압류등기 말소"},{"rank_no":"5","purpose":"가압류","receipt":"2025년 1월 9일","reason":"가압류결정","details":"청구금액 {claim_amount_secondary} 가압류권자 박준혁 수원지방법원의 가압류결정","rendering":{"claim_amount_secondary":{"source":"sale_price_ratio","ratio_percent":7,"rounding_unit":"만원"}}},{"rank_no":"6","purpose":"소유권이전등기청구권가처분","receipt":"2025년 2월 3일","reason":"가처분결정","details":"가처분권자 오세린 수원지방법원의 가처분결정"}],"issueSummary":"가압류가 반복되어 등장한 데 이어 소유권이전등기청구권가처분까지 남아 있어 분쟁 흔적이 누적된 사례입니다.","keyPoints":["가압류가 한 차례 말소된 뒤 다시 다른 가압류가 등장했습니다.","현재는 가압류와 가처분이 함께 보이는 복합 분쟁 구조입니다.","갑구 이력만으로도 권리관계가 안정적이라고 보기 어렵습니다."],"feedbackCorrect":"맞았습니다. 반복된 가압류 이력과 현재 남아 있는 가처분은 함께 볼 때 매우 강한 위험 신호입니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 말소 이력이 있더라도 분쟁 흔적이 반복되고 현재 가처분까지 남아 있어 매수인이 주의해야 합니다."}$$::jsonb),
    ('GAPGU', '갑구에 소유권보존과 소유권이전 외 특이사항이 없어 현재 등기만 보면 정상에 가까운 사례입니다.', $${"quizVerdict":"정상","rows":[{"rank_no":"1","purpose":"소유권보존","receipt":"2021년 4월 2일","reason":"보존","details":"소유자 주식회사 세림건설"},{"rank_no":"2","purpose":"소유권이전","receipt":"2024년 9월 13일","reason":"2024년 8월 30일 매매","details":"소유자 한지민"}],"issueSummary":"갑구에 소유권보존과 소유권이전 외 특이사항이 없어 현재 등기만 보면 정상에 가까운 사례입니다.","keyPoints":["갑구에 가압류, 가처분, 경매개시 같은 위험 등기가 없습니다.","말소 이력이나 반복 이전 같은 이상 흐름도 보이지 않습니다.","현재 보이는 정보만 기준으로는 비교적 깨끗한 갑구입니다."],"feedbackCorrect":"맞았습니다. 이 사례는 갑구만 보면 정상으로 판단하는 편이 자연스럽습니다.","feedbackWrong":"이번 사례는 위험보다 정상에 가깝습니다. 갑구에 소유권 외 특이사항이 보이지 않아 기본형에 가까운 샘플입니다."}$$::jsonb),
    ('EULGU', '을구에 현재 유효한 근저당권, 전세권, 임차권이 보이지 않아 매수인 기준으로는 가장 기본적인 정상형에 가까운 사례입니다.', $${"quizVerdict":"정상","rows":[],"issueSummary":"을구에 현재 유효한 근저당권, 전세권, 임차권이 보이지 않아 매수인 기준으로는 가장 기본적인 정상형에 가까운 사례입니다.","keyPoints":["을구에 현재 살아 있는 담보권이나 임차 관련 권리가 없습니다.","설정과 말소가 반복된 복잡한 이력도 보이지 않습니다.","현재 보이는 정보만 기준으로는 비교적 깨끗한 을구입니다."],"feedbackCorrect":"맞았습니다. 이 사례는 을구만 보면 정상으로 판단하는 편이 자연스럽습니다.","feedbackWrong":"이번 사례는 위험보다 정상에 가깝습니다. 을구에 현재 유효한 부담 권리가 없어 기본형에 가까운 샘플입니다."}$$::jsonb),
    ('EULGU', '현재 근저당권 1건이 보이지만 채권최고액이 매매가 대비 매우 낮고 다른 을구 권리가 없어 본 퀴즈 기준으로는 정상에 가까운 사례입니다.', $${"quizVerdict":"정상","rows":[{"rank_no":"1","purpose":"근저당권설정","receipt":"2025년 1월 17일","reason":"2025년 1월 10일 설정계약","details":"채권최고액 {max_claim_amount} 채무자 김도윤 근저당권자 주식회사 한울저축은행","rendering":{"max_claim_amount":{"source":"sale_price_ratio","ratio_percent":15,"rounding_unit":"만원"}}}],"issueSummary":"현재 근저당권 1건이 보이지만 채권최고액이 매매가 대비 매우 낮고 다른 을구 권리가 없어 본 퀴즈 기준으로는 정상에 가까운 사례입니다.","keyPoints":["현재 유효한 을구 권리는 근저당권 1건뿐입니다.","채권최고액은 매매가 대비 매우 낮은 비율로 설계되었습니다.","전세권, 임차권, 반복된 말소 이력이 함께 보이지 않습니다."],"feedbackCorrect":"맞았습니다. 이 샘플은 단독 근저당권이 남아 있지만 부담 비율이 매우 낮고 다른 권리가 없어 정상형으로 분류한 사례입니다.","feedbackWrong":"이번 사례는 위험보다 정상에 가깝습니다. 근저당권이 있더라도 비율이 매우 낮고 다른 부담 권리가 없으면 본 퀴즈 기준에서는 정상형으로 봅니다."}$$::jsonb),
    ('EULGU', '현재 근저당권은 1건이지만 채권최고액이 매매가 대비 매우 높아 매수인이 부담 규모를 크게 의식해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"근저당권설정","receipt":"2025년 2월 21일","reason":"2025년 2월 12일 설정계약","details":"채권최고액 {max_claim_amount} 채무자 박서진 근저당권자 주식회사 새론저축은행","rendering":{"max_claim_amount":{"source":"sale_price_ratio","ratio_percent":90,"rounding_unit":"만원"}}}],"issueSummary":"현재 근저당권은 1건이지만 채권최고액이 매매가 대비 매우 높아 매수인이 부담 규모를 크게 의식해야 하는 사례입니다.","keyPoints":["현재 살아 있는 근저당권이 확인됩니다.","채권최고액이 매매가 대비 매우 높은 비율로 설정돼 있습니다.","단독 권리여도 부담 규모만으로 위험 신호가 충분히 분명합니다."],"feedbackCorrect":"맞았습니다. 이 사례는 현재 근저당권 1건만 있더라도 채권최고액 비율이 매우 높아 위험으로 보는 것이 맞습니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 단독 근저당권이어도 채권최고액이 과도하게 높으면 매수인이 주의해야 하는 위험 사례입니다."}$$::jsonb),
    ('EULGU', '현재 근저당권이 2건 병존하고 있어 을구 부담 관계가 단순하지 않은 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"근저당권설정","receipt":"2024년 11월 8일","reason":"2024년 11월 1일 설정계약","details":"채권최고액 {first_max_claim_amount} 채무자 김예준 근저당권자 주식회사 다온저축은행","rendering":{"first_max_claim_amount":{"source":"sale_price_ratio","ratio_percent":50,"rounding_unit":"만원"}}},{"rank_no":"2","purpose":"근저당권설정","receipt":"2025년 1월 23일","reason":"2025년 1월 15일 설정계약","details":"채권최고액 {second_max_claim_amount} 채무자 김예준 근저당권자 주식회사 미래자산대부","rendering":{"second_max_claim_amount":{"source":"sale_price_ratio","ratio_percent":30,"rounding_unit":"만원"}}}],"issueSummary":"현재 근저당권이 2건 병존하고 있어 을구 부담 관계가 단순하지 않은 사례입니다.","keyPoints":["선순위와 후순위 근저당권이 함께 남아 있습니다.","현재 권리가 하나가 아니라 둘 이상 겹쳐 있어 정리 부담이 큽니다.","을구만 보더라도 단순한 대출 1건 수준으로 보기 어렵습니다."],"feedbackCorrect":"맞았습니다. 근저당권이 복수로 병존하는 경우는 현재 부담 관계가 복잡하므로 위험 신호로 보는 편이 맞습니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 여러 근저당권이 함께 남아 있으면 매수인은 권리 정리 가능성을 더 신중하게 확인해야 합니다."}$$::jsonb),
    ('EULGU', '현재 전세권이 그대로 남아 있어 매수인이 권리관계를 먼저 확인해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"전세권설정","receipt":"2025년 2월 10일","reason":"2025년 2월 1일 설정계약","details":"전세금 {jeonse_amount} 전세권자 오하린","rendering":{"jeonse_amount":{"source":"sale_price_ratio","ratio_percent":70,"rounding_unit":"만원"}}}],"issueSummary":"현재 전세권이 그대로 남아 있어 매수인이 권리관계를 먼저 확인해야 하는 사례입니다.","keyPoints":["을구에 현재 유효한 전세권이 기재되어 있습니다.","전세권은 단독으로도 매수인에게 명확한 부담 신호가 됩니다.","다른 권리가 없더라도 현재 전세권 존속만으로 주의가 필요합니다."],"feedbackCorrect":"맞았습니다. 현재 전세권이 남아 있는 경우는 매수인이 그대로 지나치기 어려운 대표적인 위험 신호입니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 전세권이 현재 살아 있으면 을구만 봐도 권리관계 확인이 필요한 위험 사례입니다."}$$::jsonb),
    ('EULGU', '현재 임차권등기가 남아 있어 보증금 반환 관련 부담을 의심해야 하는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"임차권등기","receipt":"2025년 3월 4일","reason":"2025년 2월 25일 임차권등기명령","details":"임차보증금 {deposit_amount} 임차권자 정시윤","rendering":{"deposit_amount":{"source":"sale_price_ratio","ratio_percent":60,"rounding_unit":"만원"}}}],"issueSummary":"현재 임차권등기가 남아 있어 보증금 반환 관련 부담을 의심해야 하는 사례입니다.","keyPoints":["임차권등기가 현재 유효하게 보입니다.","임차권은 매수인에게 직접적인 보증금 반환 이슈를 떠올리게 합니다.","을구만 보더라도 거래를 서두르기보다 현재 권리 정리를 먼저 확인해야 합니다."],"feedbackCorrect":"맞았습니다. 임차권등기가 현재 남아 있는 경우는 매수인 기준에서 매우 직접적인 위험 신호입니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 임차권등기가 남아 있다는 점만으로도 매수인은 보증금 반환 문제를 먼저 의심해야 합니다."}$$::jsonb),
    ('EULGU', '근저당권과 임차권등기가 함께 남아 있어 을구 부담 관계가 복합적인 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"근저당권설정","receipt":"2024년 10월 18일","reason":"2024년 10월 9일 설정계약","details":"채권최고액 {max_claim_amount} 채무자 윤도현 근저당권자 주식회사 청명저축은행","rendering":{"max_claim_amount":{"source":"sale_price_ratio","ratio_percent":35,"rounding_unit":"만원"}}},{"rank_no":"2","purpose":"임차권등기","receipt":"2025년 2월 28일","reason":"2025년 2월 19일 임차권등기명령","details":"임차보증금 {deposit_amount} 임차권자 강세아","rendering":{"deposit_amount":{"source":"sale_price_ratio","ratio_percent":45,"rounding_unit":"만원"}}}],"issueSummary":"근저당권과 임차권등기가 함께 남아 있어 을구 부담 관계가 복합적인 사례입니다.","keyPoints":["담보권과 임차 관련 권리가 동시에 보입니다.","각 권리가 따로 있을 때보다 매수인이 확인해야 할 범위가 더 넓습니다.","을구만 봐도 단순한 정상 매물로 보기 어려운 구조입니다."],"feedbackCorrect":"맞았습니다. 근저당권과 임차권이 함께 남아 있는 경우는 매수인이 특히 보수적으로 접근해야 하는 위험 사례입니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 현재 담보권과 임차권이 병존하면 권리 정리 부담이 분명히 커집니다."}$$::jsonb),
    ('EULGU', '근저당권이 한 차례 말소된 뒤 다시 높은 비율로 재설정되어 을구 이력이 불안정하게 보이는 사례입니다.', $${"quizVerdict":"위험","rows":[{"rank_no":"1","purpose":"근저당권설정","receipt":"2024년 6월 28일","reason":"2024년 6월 20일 설정계약","details":"채권최고액 {first_max_claim_amount} 채무자 최이준 근저당권자 주식회사 선일저축은행","rendering":{"first_max_claim_amount":{"source":"sale_price_ratio","ratio_percent":20,"rounding_unit":"만원"}}},{"rank_no":"2","purpose":"1번근저당권설정등기말소","receipt":"2024년 9월 2일","reason":"해제","details":"2024년 6월 28일 접수 근저당권설정등기 말소"},{"rank_no":"3","purpose":"근저당권설정","receipt":"2025년 1월 31일","reason":"2025년 1월 23일 설정계약","details":"채권최고액 {second_max_claim_amount} 채무자 최이준 근저당권자 주식회사 대영저축은행","rendering":{"second_max_claim_amount":{"source":"sale_price_ratio","ratio_percent":85,"rounding_unit":"만원"}}}],"issueSummary":"근저당권이 한 차례 말소된 뒤 다시 높은 비율로 재설정되어 을구 이력이 불안정하게 보이는 사례입니다.","keyPoints":["근저당권 설정과 말소 이력이 짧은 기간 안에 반복됩니다.","후속 근저당권은 매매가 대비 매우 높은 비율로 다시 설정돼 있습니다.","현재 권리 상태와 과거 이력을 함께 보면 안정적인 을구로 보기 어렵습니다."],"feedbackCorrect":"맞았습니다. 반복된 설정·말소 뒤 다시 높은 비율의 근저당권이 잡힌 경우는 매수인이 경계해야 하는 위험 사례입니다.","feedbackWrong":"이번 사례는 정상으로 보기 어렵습니다. 근저당권이 반복해서 정리되고 다시 크게 설정된 흐름은 을구 이력 자체로도 불안 신호입니다."}$$::jsonb)
)
insert into real_estate_documents (
  registry_section,
  quiz_sample_payload
)
select
  rds.registry_section,
  rds.quiz_sample_payload
from registry_document_samples rds
where not exists (
  select 1
  from real_estate_documents d
  where d.registry_section = rds.registry_section
    and d.quiz_sample_payload ->> 'issueSummary' = rds.issue_summary
);

insert into game_sessions (
  user_id,
  slot_number,
  character_name,
  character_type,
  job_type,
  housing_type,
  region_code,
  district_code,
  target_property_id,
  data_source_type,
  current_turn,
  "current_date",
  cycle_phase,
  cash_balance_amount,
  net_worth_amount,
  session_status,
  last_played_at,
  selected_card_monthly_saving_amount,
  owned_property_id
)
select
  u.user_id,
  1,
  'Homer',
  'MALE',
  'STARTUP',
  'STUDIO',
  'SEOUL',
  'GANGNAM',
  1,
  'PROFILE',
  3,
  date '2026-03-01',
  'RECOVERY',
  3200000,
  3250000,
  'IN_PROGRESS',
  timestamp '2026-03-01 09:00:00',
  0,
  null
from users u
where u.email = 'homerun@example.com'
  and not exists (
    select 1
    from game_sessions gs
    where gs.user_id = u.user_id
      and gs.slot_number = 1
  );

insert into game_cards (
  game_session_id,
  card_product_id,
  card_status_type,
  recommended_at,
  registered_at,
  active_yn
)
select
  gs.game_session_id,
  cp.card_product_id,
  'REGISTERED',
  timestamp '2026-03-01 09:00:00',
  timestamp '2026-03-01 09:00:00',
  true
from game_sessions gs
join users u on u.user_id = gs.user_id
join card_products cp on cp.card_name = '삼성 iD ON 카드'
where u.email = 'homerun@example.com'
  and gs.slot_number = 1
  and not exists (
    select 1
    from game_cards gc
    where gc.game_session_id = gs.game_session_id
      and gc.card_product_id = cp.card_product_id
      and gc.card_status_type = 'REGISTERED'
      and gc.active_yn = true
  );

with latest_month as (
  select date_trunc('month', max(payment_date))::date as month_start
  from member_payment_histories
  where user_id = (select user_id from users where email = 'homerun@example.com')
),
latest_month_payments as (
  select mph.user_id, mph.category_id, mph.payment_amount
  from member_payment_histories mph
  join latest_month lm
    on mph.payment_date >= lm.month_start
   and mph.payment_date < (lm.month_start + interval '1 month')::date
  where mph.user_id = (select user_id from users where email = 'homerun@example.com')
),
category_spend as (
  select category_id, sum(payment_amount)::numeric as total_amount
  from latest_month_payments
  group by category_id
),
selected_card as (
  select
    gs.game_session_id,
    cp.max_benefit_limit_amount,
    cp.active_benefits
  from game_sessions gs
  join users u on u.user_id = gs.user_id
  join game_cards gc
    on gc.game_session_id = gs.game_session_id
   and gc.card_status_type = 'REGISTERED'
   and gc.active_yn = true
  join card_products cp on cp.card_product_id = gc.card_product_id
  where u.email = 'homerun@example.com'
    and gs.slot_number = 1
  order by gc.registered_at desc nulls last, gc.game_card_id desc
  limit 1
),
raw_saving as (
  select
    sc.game_session_id,
    coalesce(sum(coalesce(cs.total_amount, 0) * ((benefit ->> 'discountRate')::numeric) / 100), 0::numeric) as raw_amount,
    sc.max_benefit_limit_amount
  from selected_card sc
  left join lateral jsonb_array_elements(sc.active_benefits) benefit on true
  left join category_spend cs on cs.category_id = benefit ->> 'categoryId'
  group by sc.game_session_id, sc.max_benefit_limit_amount
)
update game_sessions gs
set selected_card_monthly_saving_amount = case
  when rs.max_benefit_limit_amount is null or rs.max_benefit_limit_amount <= 0 then floor(rs.raw_amount)::integer
  else least(floor(rs.raw_amount)::integer, rs.max_benefit_limit_amount)
end
from raw_saving rs
where gs.game_session_id = rs.game_session_id;
