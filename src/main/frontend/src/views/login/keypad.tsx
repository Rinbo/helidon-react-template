import React, { useEffect, useRef, useState } from "react";
import { isSingleDigit } from "../../utils/misc-utils.ts";

const KEYS = 6;
const EMPTY_ARRAY = new Array(KEYS).fill("");

type Props = {
  submit: (keycode: string) => void;
  disabled: boolean;
  reset: boolean;
};

export default function Keypad({ submit, disabled, reset }: Props) {
  const [keycode, setKeycode] = useState<string[]>(EMPTY_ARRAY);
  const refs = useRef<HTMLInputElement[] | null[]>([]);

  useEffect(() => focusOnFirst(), []);

  useEffect(() => {
    reset && onReset();
  }, [reset]);

  useEffect(() => {
    keycode == EMPTY_ARRAY && focusOnFirst();
  }, [disabled]);

  function onReset() {
    setKeycode(EMPTY_ARRAY);
    focusOnFirst();
  }

  function focusOnFirst() {
    refs.current[0]?.focus();
  }

  function onInput(e: React.FormEvent<HTMLInputElement>, index: number) {
    const targetValue = e.currentTarget.value;
    const updatedValues = keycode.map((value, idx) => (idx === index && isSingleDigit(targetValue) ? targetValue : value));

    setKeycode(updatedValues);

    if (updatedValues.every(isSingleDigit) && !disabled) {
      submit(updatedValues.join(""));
      return;
    }

    if (isSingleDigit(targetValue) && index < KEYS - 1) {
      refs.current[index + 1]?.focus();
    }
  }

  function onPaste(e: { clipboardData: any }) {
    const clipboard = e.clipboardData;
    const data = clipboard?.getData("text");
    if (data?.length === 6 && data.split("").every((e: string) => isSingleDigit(e)) && !disabled) {
      setKeycode(data.split(""));
      submit(data);
    }
  }

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>, index: number) {
    if (e.code === "Backspace" && index > 0) {
      refs.current[index - 1]?.focus();
      const newValues = [...keycode];
      newValues[index] = "";
      newValues[index - 1] = "";
      setKeycode(newValues);
    }
  }

  return (
    <div className="flex w-full flex-col items-center">
      <form className="mt-6 flex w-full max-w-xs flex-row justify-center gap-2">
        {keycode.map((value, index) => {
          return (
            <input
              key={"input-" + index}
              maxLength={1}
              type="text"
              className={`min-w-0 flex-1 rounded border border-gray-300 p-2 text-center text-lg ${disabled && "text-gray-400"}`}
              onInput={(e) => onInput(e, index)}
              onKeyDown={(e) => onKeyDown(e, index)}
              onPaste={onPaste}
              ref={(el) => (refs.current[index] = el)}
              value={value}
              pattern="[0-9]"
              inputMode="numeric"
              disabled={disabled}
            />
          );
        })}
      </form>
    </div>
  );
}
