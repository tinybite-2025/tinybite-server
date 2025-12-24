-- 만 14세 이상
INSERT INTO terms (title, description, required, version)
VALUES ('AGE_OVER_14', '본 서비스는 만 14세 이상만 이용 가능합니다.', true, 1);

-- 서비스 이용약관
INSERT INTO terms (title, description, required, version)
VALUES ('SERVICE_USE', '서비스 이용을 위한 기본 약관입니다.', true, 1);

-- 전자금융거래 이용약관
INSERT INTO terms (title, description, required, version)
VALUES ('ELECTRONIC_FINANCE', '전자금융거래 관련 약관입니다.', true, 1);

-- 개인정보 수집 이용
INSERT INTO terms (title, description, required, version)
VALUES ('PRIVACY_COLLECT', '서비스 제공을 위한 개인정보 수집 및 이용에 대한 동의입니다.', true, 1);

-- 개인정보 제공
INSERT INTO terms (title, description, required, version)
VALUES ('PRIVACY_PROVIDE', '제3자에게 개인정보를 제공하는 것에 대한 동의입니다.', true, 1);

-- 마케팅 정보 수신
INSERT INTO terms (title, description, required, version)
VALUES ('MARKETING_RECEIVE', '이벤트 및 혜택 정보 수신에 대한 동의입니다.', false, 1);