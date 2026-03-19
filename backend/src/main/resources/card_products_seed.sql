-- Seed data for card_products.
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
-- Card names are representative seed data, and benefit rates are normalized
-- for this project's game/recommendation use rather than copied verbatim.

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
    '생활, 교통, 해외 영역을 폭넓게 커버하는 대표 생활형 카드',
    400000,
    35000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 12.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 10.0,
        "exampleMerchants": []
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 8.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-kb-my-wesh-front.png',
    true
  ),
  (
    '굿데이올림카드',
    'KB국민카드',
    '주유, 마트, 통신 등 고정지출 절감에 맞춘 실속형 카드',
    300000,
    40000,
    '[
      {
        "categoryId": "CG-3fa85f6425e811e",
        "categoryName": "주유",
        "categoryDescription": "",
        "discountRate": 8.0,
        "exampleMerchants": ["SK 에너지"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 10.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 10.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      }
    ]'::jsonb,
    'card-kb-goodday-olim-front.png',
    true
  ),
  (
    'toss KB국민카드',
    'KB국민카드',
    '기본 적립에 생활 가맹점 추가 적립을 얹은 범용 포인트 카드',
    0,
    25000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 4.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 3.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 2.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-kb-toss-front.png',
    true
  ),
  (
    'KB국민 청춘대로 톡톡카드',
    'KB국민카드',
    '생활 편의 업종과 교통 혜택을 강화한 청년층 중심 카드',
    300000,
    30000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 10.0,
        "exampleMerchants": []
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 7.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      }
    ]'::jsonb,
    'card-kb-cheongchun-toktok-front.png',
    true
  ),
  (
    'KB국민 톡톡 my point 카드',
    'KB국민카드',
    '생활 소비 중심 적립형으로 마트와 해외 사용도 일부 커버하는 카드',
    300000,
    30000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 8.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 5.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 5.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-kb-toktok-my-point-front.png',
    true
  ),
  (
    '삼성카드 taptap O',
    '삼성카드',
    '생활, 교통, 해외에 고르게 혜택을 배치한 대표 할인 카드',
    300000,
    40000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 10.0,
        "exampleMerchants": []
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 5.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-samsung-taptap-o-front.png',
    true
  ),
  (
    '삼성 iD ON 카드',
    '삼성카드',
    '일상 소비와 통신비, 대중교통 할인에 집중한 생활 밀착형 카드',
    300000,
    40000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 12.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 10.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 8.0,
        "exampleMerchants": []
      }
    ]'::jsonb,
    'card-samsung-id-on-front.png',
    true
  ),
  (
    '삼성 iD ALL 카드',
    '삼성카드',
    '마트, 생활, 해외 전반을 고르게 챙기는 범용형 카드',
    500000,
    50000,
    '[
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 7.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      },
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 7.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 5.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-samsung-id-all-front.png',
    true
  ),
  (
    '삼성카드 & MILEAGE PLATINUM',
    '삼성카드',
    '해외 이용 비중이 높은 사용자를 위한 마일리지 성격의 여행형 카드',
    500000,
    60000,
    '[
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 10.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 5.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 3.0,
        "exampleMerchants": []
      }
    ]'::jsonb,
    'card-samsung-mileage-platinum-front.png',
    true
  ),
  (
    '삼성 iD VITA 카드',
    '삼성카드',
    '생활비와 교육비, 차량 관련 지출을 두루 고려한 가족형 카드',
    300000,
    35000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-6dd85f6425ez11o",
        "categoryName": "교육/육아",
        "categoryDescription": "",
        "discountRate": 7.0,
        "exampleMerchants": []
      },
      {
        "categoryId": "CG-3fa85f6425e811e",
        "categoryName": "주유",
        "categoryDescription": "",
        "discountRate": 5.0,
        "exampleMerchants": ["SK 에너지"]
      }
    ]'::jsonb,
    'card-samsung-id-vita-front.png',
    true
  ),
  (
    '디지로카 London',
    '롯데카드',
    '생활, 교통, 대형마트를 강하게 묶은 롯데 대표 생활 할인 카드',
    700000,
    130000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 20.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 10.0,
        "exampleMerchants": []
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 5.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      }
    ]'::jsonb,
    'card-lotte-digiroca-london-front.png',
    true
  ),
  (
    'LOCA LIKIT 1.2',
    '롯데카드',
    '무난한 적립 구조에 생활과 해외 소비를 더한 범용 카드',
    0,
    30000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 5.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 5.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 3.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      }
    ]'::jsonb,
    'card-lotte-loca-likit-1-2-front.png',
    true
  ),
  (
    'LOCA 365 카드',
    '롯데카드',
    '월 고정지출과 생활 편의 업종을 중심으로 할인받는 카드',
    500000,
    36000,
    '[
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 10.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      },
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 5.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      }
    ]'::jsonb,
    'card-lotte-loca-365-front.png',
    true
  ),
  (
    '디지로카 Paris',
    '롯데카드',
    '해외와 생활 혜택을 함께 주는 여행 소비 특화 카드',
    500000,
    70000,
    '[
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 12.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 7.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 5.0,
        "exampleMerchants": []
      }
    ]'::jsonb,
    'card-lotte-digiroca-paris-front.png',
    true
  ),
  (
    'LOCA Professional',
    '롯데카드',
    '주유, 통신, 해외 소비를 넓게 아우르는 고실적형 카드',
    800000,
    100000,
    '[
      {
        "categoryId": "CG-3fa85f6425e811e",
        "categoryName": "주유",
        "categoryDescription": "",
        "discountRate": 10.0,
        "exampleMerchants": ["SK 에너지"]
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 8.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 8.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      }
    ]'::jsonb,
    'card-lotte-loca-professional-front.png',
    true
  ),
  (
    '신한카드 Mr.Life',
    '신한카드',
    '생활비, 통신비, 장보기 소비 절감에 강한 대표 생활비 카드',
    300000,
    40000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 10.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 10.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      }
    ]'::jsonb,
    'card-shinhan-mr-life-front.png',
    true
  ),
  (
    '신한카드 Point Plan',
    '신한카드',
    '생활 중심 포인트 적립에 해외와 통신 영역을 더한 카드',
    400000,
    50000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 7.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 7.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-7fa85f6425bc311",
        "categoryName": "통신",
        "categoryDescription": "(전화요금, 인터넷 이용료, 케이블TV 업종)",
        "discountRate": 5.0,
        "exampleMerchants": ["SKT", "LG 유플러스"]
      }
    ]'::jsonb,
    'card-shinhan-point-plan-front.png',
    true
  ),
  (
    '신한 TRAVEL 카드',
    '신한카드',
    '해외 결제 혜택을 중심으로 구성한 여행 특화 카드',
    100000,
    100000,
    '[
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 15.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-4fa85f6455cad4a",
        "categoryName": "교통",
        "categoryDescription": "(버스, 지하철, 택시)",
        "discountRate": 5.0,
        "exampleMerchants": []
      }
    ]'::jsonb,
    'card-shinhan-travel-front.png',
    true
  ),
  (
    '신한카드 RPM+ Platinum#',
    '신한카드',
    '주유 할인 중심에 해외와 생활 사용까지 고려한 차량 특화 카드',
    500000,
    50000,
    '[
      {
        "categoryId": "CG-3fa85f6425e811e",
        "categoryName": "주유",
        "categoryDescription": "",
        "discountRate": 12.0,
        "exampleMerchants": ["SK 에너지"]
      },
      {
        "categoryId": "CG-8fa85f6425e1123",
        "categoryName": "해외",
        "categoryDescription": "(해외직구)",
        "discountRate": 5.0,
        "exampleMerchants": ["알리 익스프레스", "아마존 익스프레스"]
      },
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 3.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      }
    ]'::jsonb,
    'card-shinhan-rpm-plus-front.png',
    true
  ),
  (
    '신한카드 Deep Dream',
    '신한카드',
    '생활 소비 중심 적립과 마트, 교육비 보조를 섞은 대표 포인트 카드',
    300000,
    45000,
    '[
      {
        "categoryId": "CG-9ca85f66311a23d",
        "categoryName": "생활",
        "categoryDescription": "(음식점, 커피전문점, 편의점, 약국 ..)",
        "discountRate": 10.0,
        "exampleMerchants": ["스타벅스", "김밥천국", "뚜레쥬르"]
      },
      {
        "categoryId": "CG-4fa85f6425ad1d3",
        "categoryName": "대형마트",
        "categoryDescription": "",
        "discountRate": 7.0,
        "exampleMerchants": ["코스트코", "홈플러스"]
      },
      {
        "categoryId": "CG-6dd85f6425ez11o",
        "categoryName": "교육/육아",
        "categoryDescription": "",
        "discountRate": 5.0,
        "exampleMerchants": []
      }
    ]'::jsonb,
    'card-shinhan-deep-dream-front.png',
    true
  );
