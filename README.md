# HolidayApplication
Android開発での祝日の取得サンプルコードです。

Androidで祝日を取得する方法として以下の三つを考えました。

- 1. 内閣府が提供するcsvを取り込む方法 -> 固定のデータでは将来変更あったときめんどくさい&日本しか取得できない
  - https://www8.cao.go.jp/chosei/shukujitsu/gaiyou.html
- 2. googleapiを叩く方法 -> Googleの認証が必要で認証が必要で処理が煩雑になるなら3.使用した方が良さげ
  - https://stackoverflow.com/questions/18996577/how-to-get-national-holidays-of-selected-country
- 3. CalendatProviderから、端末にログインしているGoogleアカウントのカレンダーに登録されている祝日を取得

今回は下記の理由から3を採択しました
- 固定データは変更があったときめんどくさい
- 日本以外の祝日も取得したい

## setup

1. 端末にGoogleアカウントでログインする
2. 1.でログインしたアカウントのカレンダーに祝日を追加する

上記の1,2を実行した後、`HolidayProvider`クラスから祝日を取得できます。(取得できるようになるまでに時間がかかる場合があります。)
