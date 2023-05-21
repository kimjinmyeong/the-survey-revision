import { AxiosError } from 'axios';
import { Dispatch } from 'redux';

import { setLoggedIn, HeaderAction } from '../types/header';

export const responseErrorHandle = (error: AxiosError, dispatch: Dispatch<HeaderAction>): string[] => {
  const { response } = error as AxiosError;

  let labelText = `${response?.data}`;
  let buttonText = '';
  let navigateRoute = '';

  switch (response?.status) {
    case 400:
      break;
    case 401:
      if (labelText === '설문조사에 필요한 인증을 하지 않았습니다.') {
        buttonText = '인증 하러 가기';
        navigateRoute = '/mypage/auth-list';
      } else {
        dispatch(setLoggedIn(false));
        labelText = '로그인이 만료 되었습니다.';
        buttonText = '로그인 하러 가기';
        navigateRoute = '/login';
      }
      break;
    case 403:
      buttonText = '설문 리스트로 돌아가기';
      navigateRoute = '/survey';
      break;
    case 500:
      labelText = '서버에 문제가 생겼습니다.';
      navigateRoute = '/';
      break;
    default:
      buttonText = '홈화면으로 돌아가기';
      navigateRoute = '/';
      break;
  }

  return [labelText, buttonText, navigateRoute];
};
