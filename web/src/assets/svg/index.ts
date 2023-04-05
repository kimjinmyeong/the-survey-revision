import { ReactComponent as Arrow } from './arrow.svg';
import { ReactComponent as DriverLicense } from './CertificationIcons/DriverLicense.svg';
import { ReactComponent as Google } from './CertificationIcons/Google.svg';
import { ReactComponent as Id } from './CertificationIcons/IdCard.svg';
import { ReactComponent as Kakao } from './CertificationIcons/Kakao.svg';
import { ReactComponent as MobilePhone } from './CertificationIcons/MobilePhone.svg';
import { ReactComponent as Naver } from './CertificationIcons/Naver.svg';
import { ReactComponent as Webmail } from './CertificationIcons/Webmail.svg';
import { ReactComponent as Free } from './CertificationIcons/X.svg';
import { ReactComponent as Chart } from './chartImage.svg';
import { ReactComponent as Check } from './Check-Icon.svg';
import { ReactComponent as Error } from './error-icon.svg';
import { ReactComponent as Favicon } from './favicon.svg';
import { ReactComponent as Info } from './info-icon.svg';
import { ReactComponent as DarkLogo } from './logo-dark.svg';
import { ReactComponent as LightLogo } from './logo-light.svg';
import { ReactComponent as Pencil } from './pencil.svg';
import { ReactComponent as TwoArrow } from './twoArrow.svg';
import { ReactComponent as UserImage } from './userBasicImage.svg';
import { ReactComponent as Warn } from './warn-icon.svg';

export type IconType = React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string | undefined }>;

export const Icons: { [src: string]: IconType } = {
  DRIVER_LICENSE: DriverLicense,
  ERROR: Error,
  FAVICON: Favicon,
  GOOGLE: Google,
  ID: Id,
  INFO: Info,
  KAKAO: Kakao,
  MOBILE_PHONE: MobilePhone,
  DARK_LOGO: DarkLogo,
  LIGHT_LOGO: LightLogo,
  NAVER: Naver,
  WARN: Warn,
  WEBMAIL: Webmail,
  CHECK: Check,
  ARROW: Arrow,
  PENCIL: Pencil,
  TWOARROW: TwoArrow,
  CHART: Chart,
  USERIMAGE: UserImage,
  FREE: Free,
};
