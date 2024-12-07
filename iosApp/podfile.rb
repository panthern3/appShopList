platform :ios, '10'

target 'iosApp' do
  use_frameworks!


# Отключаем кодовую подпись
ENV['CODE_SIGNING_REQUIRED'] = 'NO'
ENV['CODE_SIGN_IDENTITY'] = 'iPhone Developer'

end