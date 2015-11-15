# wlanAutoCtlrByBle

## 目的
- 自宅ではWLAN ON、外出中ではWLAN OFFに自動的に切替ることで消費電力を抑える(自分用)
- BLE勉強

## 手段
- 自宅にあるBLEビーコンを一定間隔で監視し、特定期間BLEビーコンをロストしたらWLAN OFFに遷移させる
- 自宅にあるBLEビーコンを獲得したらWLAN ONに遷移させる
