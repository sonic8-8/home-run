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
select
  'SEOCHO-ART-XI',
  '서초아트자이',
  '서울특별시 서초구 반포대로 58',
  'SEOUL',
  'SEOCHO',
  1300000000,
  37.485551,
  127.011500,
  'OWNED_APT',
  null
where not exists (
  select 1
  from real_estate_properties
  where provider_id = 'SEOCHO-ART-XI'
);

with target_property as (
  select property_id
  from real_estate_properties
  where provider_id = 'SEOCHO-ART-XI'
), registry_document_samples(registry_section, issue_summary, quiz_sample_payload) as (
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
  property_id,
  document_type,
  registry_section,
  quiz_sample_payload
)
select
  tp.property_id,
  'REGISTRY',
  rds.registry_section,
  rds.quiz_sample_payload
from target_property tp
cross join registry_document_samples rds
where not exists (
  select 1
  from real_estate_documents d
  where d.property_id = tp.property_id
    and d.document_type = 'REGISTRY'
    and d.registry_section = rds.registry_section
    and d.quiz_sample_payload ->> 'issueSummary' = rds.issue_summary
);

insert into game_sessions (
  user_id,
  slot_number,
  character_name,
  character_type,
  job_type,
  housing_type,
  target_region_code,
  target_district_code,
  data_source_type,
  current_turn,
  "current_date",
  economic_cycle_type,
  cash,
  net_assets,
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
  'DUMMY',
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

insert into financial_product_templates (
  product_type,
  institution_name,
  product_name,
  active_yn
)
select
  candidate.product_type,
  candidate.institution_name,
  candidate.product_name,
  candidate.active_yn
from (
  values
    ('SAVING_DEPOSIT', 'KB국민은행', 'KB Star 정기예금', true),
    ('SAVING_DEPOSIT', 'KB국민은행', 'KB Star 자유적금', true),
    ('SAVING_DEPOSIT', 'KB국민은행', 'KB내맘대로적금', true),
    ('SAVING_DEPOSIT', '신한은행', '쏠편한 정기예금', true),
    ('SAVING_DEPOSIT', '신한은행', '쏠편한 작심3일 적금', true),
    ('SAVING_DEPOSIT', '신한은행', '신한 안녕, 반가워 적금', true),
    ('SAVING_DEPOSIT', '하나은행', '하나의정기예금', true),
    ('SAVING_DEPOSIT', '하나은행', '급여하나 월복리 적금', true),
    ('SAVING_DEPOSIT', '하나은행', '하나 원큐 적금', true),
    ('SAVING_DEPOSIT', '우리은행', 'WON플러스 예금', true),
    ('SAVING_DEPOSIT', '우리은행', '우리 퍼스트 정기적금', true),
    ('SAVING_DEPOSIT', '우리은행', '우리 청년우대형 적금', true),
    ('SAVING_DEPOSIT', 'NH농협은행', 'NH올원e예금', true),
    ('SAVING_DEPOSIT', 'NH농협은행', 'NH직장인월복리적금', true),
    ('SAVING_DEPOSIT', 'NH농협은행', 'NH1934월복리적금', true),
    ('SAVING_DEPOSIT', '카카오뱅크', '카카오뱅크 정기예금', true),
    ('SAVING_DEPOSIT', '카카오뱅크', '카카오뱅크 자유적금', true),
    ('SAVING_DEPOSIT', '카카오뱅크', '카카오뱅크 26주적금', true),
    ('SAVING_DEPOSIT', '케이뱅크', 'codeK 정기예금', true),
    ('SAVING_DEPOSIT', '케이뱅크', 'codeK 자유적금', true),
    ('SAVING_DEPOSIT', '케이뱅크', '궁금한 적금', true),
    ('SAVING_DEPOSIT', '토스뱅크', '먼저 이자 받는 정기예금', true),
    ('SAVING_DEPOSIT', '토스뱅크', '키워봐요 적금', true),
    ('SAVING_DEPOSIT', '토스뱅크', '토스뱅크 자유적금', true),
    ('INVESTMENT', '키움증권', '국내주식 위탁종합계좌', true),
    ('INVESTMENT', '키움증권', '영웅문 글로벌 투자계좌', true),
    ('INVESTMENT', '한국투자증권', '뱅키스 주식거래계좌', true),
    ('INVESTMENT', '한국투자증권', 'CMA 종합자산계좌', true),
    ('INVESTMENT', '미래에셋증권', '다이렉트 주식거래계좌', true),
    ('INVESTMENT', '미래에셋증권', 'CMA-RP 네이버통장', true),
    ('INVESTMENT', 'NH투자증권', '나무 종합매매계좌', true),
    ('INVESTMENT', 'NH투자증권', '나무 CMA', true),
    ('INVESTMENT', '삼성증권', '종합주식거래계좌', true),
    ('INVESTMENT', '삼성증권', '연금저축 투자계좌', true),
    ('INVESTMENT', 'KB증권', 'able 주식종합계좌', true),
    ('INVESTMENT', 'KB증권', 'able CMA', true),
    ('INVESTMENT', '신한투자증권', '알파 주식거래계좌', true),
    ('INVESTMENT', '신한투자증권', 'CMA RP형 계좌', true),
    ('INVESTMENT', '대신증권', '크레온 주식종합계좌', true),
    ('INVESTMENT', '토스증권', '국내주식 거래계좌', true),
    ('LOAN', 'KB국민은행', 'KB 직장인든든 신용대출', true),
    ('LOAN', 'KB국민은행', 'KB 비상금대출', true),
    ('LOAN', 'KB국민은행', 'KB 전세금안심대출', true),
    ('LOAN', 'KB국민은행', 'KB 주택담보대출', true),
    ('LOAN', '신한은행', '쏠편한 직장인대출S', true),
    ('LOAN', '신한은행', '쏠편한 비상금대출', true),
    ('LOAN', '신한은행', '신한 전세자금대출', true),
    ('LOAN', '신한은행', '신한 주택담보대출', true),
    ('LOAN', '우리은행', 'WON플러스 직장인대출', true),
    ('LOAN', '우리은행', '우리 비상금대출', true),
    ('LOAN', '우리은행', '우리WON전세대출', true),
    ('LOAN', '우리은행', '우리WON주택대출', true),
    ('LOAN', 'NH농협은행', 'NH직장인대출V', true),
    ('LOAN', 'NH농협은행', 'NH올원 비상금대출', true),
    ('LOAN', 'NH농협은행', 'NH전세대출', true),
    ('LOAN', 'NH농협은행', 'NH주택담보대출', true),
    ('LOAN', '카카오뱅크', '카카오뱅크 비상금대출', true),
    ('LOAN', '카카오뱅크', '카카오뱅크 신용대출', true),
    ('LOAN', '케이뱅크', '케이뱅크 신용대출 플러스', true),
    ('LOAN', '토스뱅크', '토스뱅크 신용대출', true)
) as candidate(product_type, institution_name, product_name, active_yn)
where not exists (
  select 1
  from financial_product_templates template
  where template.product_type = candidate.product_type
    and template.institution_name = candidate.institution_name
    and template.product_name = candidate.product_name
);

insert into stock_markets (
  stock_code,
  stock_name,
  kis_stock_code,
  sector,
  base_price_amount,
  year_low_price_amount,
  year_high_price_amount,
  volatility_rate
)
select
  candidate.stock_code,
  candidate.stock_name,
  candidate.kis_stock_code,
  candidate.sector,
  candidate.base_price_amount,
  candidate.year_low_price_amount,
  candidate.year_high_price_amount,
  candidate.volatility_rate
from (
  values
    ('005930', '삼성전자', '005930', '반도체', 186200, 52900, 223000, 0.0215),
    ('000660', 'SK하이닉스', '000660', '반도체', 979000, 162700, 1099000, 0.0280),
    ('005380', '현대차', '005380', '자동차', 491000, 175800, 687000, 0.0220),
    ('000270', '기아', '000270', '자동차', 158100, 81300, 212500, null),
    ('035420', 'NAVER', '035420', '플랫폼', 212500, 176200, 295000, 0.0240),
    ('035720', '카카오', '035720', '플랫폼', 47700, 36300, 71600, 0.0310),
    ('207940', '삼성바이오로직스', '207940', '바이오', 1629000, 1501000, 1987000, 0.0175),
    ('068270', '셀트리온', '068270', '바이오', 194700, 144615, 251000, null),
    ('373220', 'LG에너지솔루션', '373220', '2차전지', 388500, 266000, 527000, null),
    ('055550', '신한지주', '055550', '금융', 89100, 42500, 107200, null),
    ('028260', '삼성물산', '028260', '산업재', 277500, 108100, 364000, null),
    ('034020', '두산에너빌리티', '034020', '에너지', 99800, 19960, 112100, null),
    ('012450', '한화에어로스페이스', '012450', '방산', 1328000, 603000, 1655000, null),
    ('005490', 'POSCO홀딩스', '005490', '철강/소재', 337000, 230000, 427500, null),
    ('105560', 'KB금융', '105560', '금융', 148800, 69300, 172500, null),
    ('086790', '하나금융지주', '086790', '금융', 111500, 51500, 133700, null),
    ('032830', '삼성생명', '032830', '보험', 217000, 73300, 259500, null),
    ('138040', '메리츠금융지주', '138040', '금융', 112700, 99700, 149800, null),
    ('017670', 'SK텔레콤', '017670', '통신', 79800, 50400, 88600, null),
    ('030200', 'KT', '030200', '통신', 65800, 44550, 70500, null),
    ('066570', 'LG전자', '066570', '가전', 97600, 59500, 127500, null),
    ('006400', '삼성SDI', '006400', '2차전지', 248500, 130000, 302000, null),
    ('051910', 'LG화학', '051910', '화학/소재', 363000, 196000, 425500, null),
    ('003670', '포스코퓨처엠', '003670', '2차전지', 120400, 107100, 221000, null),
    ('042700', '한미반도체', '042700', '반도체', 101100, 56200, 156600, null),
    ('000810', '삼성화재', '000810', '보험', 544000, 264500, 576000, null),
    ('316140', '우리금융지주', '316140', '금융', 20150, 13330, 21300, null),
    ('329180', 'HD현대중공업', '329180', '조선/기계', 403000, 200500, 464500, null),
    ('042660', '한화오션', '042660', '조선/기계', 134300, 24950, 137600, null),
    ('064350', '현대로템', '064350', '방산', 135900, 41350, 143900, null),
    ('267260', 'HD현대일렉트릭', '267260', '에너지', 465000, 172000, 509000, null),
    ('000720', '현대건설', '000720', '건설', 79200, 29300, 85800, null),
    ('011200', 'HMM', '011200', '물류', 31950, 15500, 33950, null),
    ('009540', 'HD한국조선해양', '009540', '조선/기계', 433500, 119400, 435000, null),
    ('018260', '삼성SDS', '018260', 'IT서비스', 228000, 102400, 245500, null),
    ('096770', 'SK이노베이션', '096770', '에너지', 99000, 78300, 161700, null),
    ('010130', '고려아연', '010130', '철강/소재', 1139000, 510000, 1139000, null),
    ('003550', 'LG', '003550', '지주', 73500, 65000, 95400, null),
    ('010950', 'S-Oil', '010950', '에너지', 93500, 38000, 98600, null),
    ('352820', '하이브', '352820', '엔터', 249500, 190500, 320500, null),
    ('051900', 'LG생활건강', '051900', '소비재', 415000, 227000, 424500, null),
    ('139480', '이마트', '139480', '유통', 94500, 57200, 99200, null),
    ('097950', 'CJ제일제당', '097950', '소비재', 388500, 196500, 409500, null),
    ('000100', '유한양행', '000100', '바이오', 184200, 116400, 193100, null),
    ('323410', '카카오뱅크', '323410', '금융', 49800, 18200, 50300, null),
    ('086280', '현대글로비스', '086280', '물류', 278500, 105700, 337500, null),
    ('090430', '아모레퍼시픽', '090430', '소비재', 138600, 81900, 158800, null),
    ('271560', '오리온', '271560', '소비재', 135600, 88300, 169300, null),
    ('034730', 'SK', '034730', '지주', 153100, 111800, 166800, null),
    ('015760', '한국전력', '015760', '유틸리티', 42500, 17110, 43450, null)
) as candidate(
  stock_code,
  stock_name,
  kis_stock_code,
  sector,
  base_price_amount,
  year_low_price_amount,
  year_high_price_amount,
  volatility_rate
)
where not exists (
  select 1
  from stock_markets market
  where market.stock_code = candidate.stock_code
);
