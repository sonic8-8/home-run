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
