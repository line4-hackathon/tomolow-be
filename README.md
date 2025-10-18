## 🎯 Git Convention

- 🎉 **Start:** Start New Project [:tada:]
- ✨ **Feat:** 새로운 기능을 추가 [:sparkles:]
- 🐛 **Fix:** 버그 수정 [:bug:]
- 🎨 **Design:** CSS 등 사용자 UI 디자인 변경 [:art:]
- ♻️ **Refactor:** 코드 리팩토링 [:recycle:]
- 🔧 **Settings:** Changing configuration files [:wrench:]
- 🗃️ **Comment:** 필요한 주석 추가 및 변경 [:card_file_box:]
- ➕ **Dependency/Plugin:** Add a dependency/plugin [:heavy_plus_sign:]
- 📝 **Docs:** 문서 수정 [:memo:]
- 🔀 **Merge:** Merge branches [:twisted_rightwards_arrows:]
- 🚀 **Deploy:** Deploying stuff [:rocket:]
- 🚚 **Rename:** 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 [:truck:]
- 🔥 **Remove:** 파일을 삭제하는 작업만 수행한 경우 [:fire:]
- ⏪️ **Revert:** 전 버전으로 롤백 [:rewind:]

- ⭐️ commit ex > git commit -m ":sparkles: 추가 기능 내용"

## 🪴 Branch Convention (GitHub Flow)

- `main`: 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `feat/{이슈번호}-{description}`: 새로운 기능을 개발하는 브랜치
    - 예: `feat/23-users-login`

## 💡 Flow

1. issue를 등록한다.
2. Branch Convention에 맞게 Branch를 생성한다.
3. add - commit - push 의 과정을 거친다.
4. Github에서 Pull Request를 작성해 생성하고, 해당 PR에 관한 리뷰를 요청한다.
5. Approve를 받았다면, Merge를 진행한다.
6. merge된 Branch를 삭제하거나 관리한다.
7. 종료된 Issue와 Pull Request의 Label과 Project를 관리한다.
