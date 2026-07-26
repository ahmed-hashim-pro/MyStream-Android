# Screen baselines

Captures produced by `scripts/screen_audit.py`. Commit them — a diff against a
committed baseline is what turns "did this shift?" into a yes/no answer instead
of a judgement call.

    python3 scripts/screen_audit.py capture more_en_light   # grab png + view tree
    python3 scripts/screen_audit.py audit   more_en_light   # measure, don't look
    python3 scripts/screen_audit.py diff    a b             # pixel diff

Capture one baseline per variant: `<screen>_<locale>_<theme>`.
The app overrides device locale, so switch it with:

    adb shell cmd locale set-app-locales com.medoapps.www.onlinequran --locales en-US
