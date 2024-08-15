<p align="center">
  <a href="https://capstone-2023-40-thesurvey.vercel.app">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="docs/assets/logo-dark.webp">
      <img src="docs/assets/logo-light.webp" height="128">
    </picture>
  </a>
  <h1 align="center">설문조사 플랫폼, the <span style="color:orange">s</span>urvey</h1>
</p>

![image](https://github.com/user-attachments/assets/a477d2e8-5014-4b7b-ad9b-421fcc3930f5)


## 프로젝트 소개

'**the survey**'는 설문조사를 생성하고 참여할 수 있는 플랫폼입니다.

**무분별한 설문조사 게시를 방지**하기 위해, 게시를 위해서는 일정 포인트가 필요하며, <u>**설문조사 참여에 대한 보상**</u>으로 포인트를 얻을 수 있습니다.

## 프로젝트 구조도
![image](https://github.com/user-attachments/assets/df603ec7-a43d-452f-9de6-eb392dce23a3)

## 구현된 기능

### 마이 페이지
- **기능 설명**: 사용자 개인의 정보를 확인하고 수정할 수 있는 페이지입니다.
- **주요 기능**:
  - 개인 정보 수정: 이름, 이메일 등 개인 정보를 업데이트할 수 있습니다.
  - 참여한 설문조사 내역: 사용자가 참여한 설문조사의 기록을 볼 수 있습니다.
  - 포인트 조회: 사용자가 설문조사 참여를 통해 획득한 포인트를 확인할 수 있습니다.

### 설문조사 목록

설문조사가 최신순으로 정렬된 목록을 보여줍니다.

### 설문조사 생성
- **기능 설명**: 사용자가 새로운 설문조사를 생성할 수 있는 기능을 제공합니다.
- **주요 기능**:
  - 주관식 질문 생성: 장문형과 단답형 주관식 질문을 추가할 수 있습니다.
  - 객관식 질문 생성: 여러 선택지를 제공하여 객관식 질문을 구성할 수 있습니다.

### 포인트 시스템
- **기능 설명**: 설문조사에 참여하거나 설문조사를 생성하여 포인트를 획득할 수 있는 시스템입니다.
- **초기 포인트**: 회원가입 시 50 포인트 지급
- **설문조사 유형 별 필요 포인트 및 획득 포인트**
  - **포인트 획득**:
    - 단답형 질문 참여 시: 1 포인트 획득
    - 객관식(단일 선택) 질문 참여 시: 1 포인트 획득
    - 객관식(복수 선택) 질문 참여 시: 2 포인트 획득
    - 주관식(단답형) 질문 참여 시: 2 포인트 획득
    - 주관식(장문형) 질문 참여 시: 3 포인트 획득
  - **포인트 사용**:
    - 단답형 질문 생성 시: 2 포인트 소모
    - 객관식(단일 선택) 질문 생성 시: 2 포인트 소모
    - 객관식(복수 선택) 질문 생성 시: 4 포인트 소모
    - 주관식(단답형) 질문 생성 시: 4 포인트 소모
    - 주관식(장문형) 질문 생성 시: 6 포인트 소모
   
### 미구현
- 설문조사 결과를 조회하는 페이지가 구현되어 있지 않습니다.
- 설문조사에 인증 필요하도록 설정할 수 있으나, 실제로 OAuth2.0을 사용한 인증 수단이 구현되어 있지 않습니다. 따라서 마이페이지에서 OAuth2.0을 사용하지 않고 인증 처리가 가능하도록 구현되어 있습니다.

## 팀 소개
<table>
  <thead align="center">
    <tr align="center">
      <th align="center" style="text-align: center;">
        <a href="https://github.com/SeiwonPark">박세원</a>
      </th>
      <th align="center" style="text-align: center;">
        <a href="https://github.com/kimjinmyeong">김진명</a>
      </th>
      <th align="center" style="text-align: center;">
        <a href="https://github.com/ohsung1125">오성혁</a>
      </th>
      <th align="center" style="text-align: center;">
        <a href="https://github.com/sanghyun1128">이상현</a>
      </th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/SeiwonPark">
          <img src="https://avatars.githubusercontent.com/SeiwonPark" alt="SeiwonPark" width="128" height="128">
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/kimjinmyeong">
          <img src="https://avatars.githubusercontent.com/kimjinmyeong" alt="kimjinmyeong" width="128" height="128">
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/ohsung1125">
          <img src="https://avatars.githubusercontent.com/ohsung1125" alt="ohsung1125" width="128" height="128">
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/sanghyun1128">
          <img src="https://avatars.githubusercontent.com/sanghyun1128" alt="sanghyun1128" width="128" height="128">
        </a>
      </td>
    </tr>
    <tr align="center">
      <td align="center" style="text-align: center;">
        ****3178
      </td>
      <td align="center" style="text-align: center;">
        ****1599
      </td>
      <td align="center" style="text-align: center;">
        ****1247
      </td>
      <td align="center" style="text-align: center;">
        ****1657
      </td>
    </tr>
    <tr>
      <td align="left">
        <ul style="list-style-type:'- ';padding-left:0;">
          <li>PM</li>
          <li>Backend</li>
        </ul>
      </td>
      <td align="left">
        <ul style="list-style-type:'- ';padding-left:0;">
          <li>Backend</li>
          <li>DevOps</li>
        </ul>
      </td>
      <td align="left">
        <ul style="list-style-type:'- ';padding-left:0;">
          <li>Frontend</li>
          <li>UI/UX</li>
        </ul>
      </td>
      <td align="left">
        <ul style="list-style-type:'- ';padding-left:0;">
          <li>Frontend</li>
          <li>UI/UX</li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

<br/>   
