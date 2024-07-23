import TextInput from "../../components/form/text-input.tsx";
import React from "react";
import { z } from "zod";
import { LoginState } from "./login-wrapper.tsx";
import { Link, useFetcher } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { FaLock } from "react-icons/fa";

type Props = {
  setState: React.Dispatch<React.SetStateAction<LoginState>>;
  setEmail: React.Dispatch<React.SetStateAction<string>>;
};

const schema = z.object({
  email: z.string().email(),
});

type Schema = z.infer<typeof schema>;

export default function EmailInsert({ setState, setEmail }: Props) {
  const fetcher = useFetcher();
  const { register, handleSubmit, formState } = useForm<Schema>({ resolver: zodResolver(schema) });

  function onSubmit(data: Schema) {
    fetcher.submit(data, { method: "post", action: "/login", encType: "application/json" });
    setEmail(data.email);
    setState("passcode-insert");
  }

  return (
    <div className="flex flex-col items-center justify-center gap-4">
      <FaLock size={55} className="text-secondary" />
      <h1 className="text-lg uppercase">Login</h1>
      <fetcher.Form className="flex w-full max-w-md flex-col gap-2" onSubmit={handleSubmit(onSubmit)}>
        <TextInput register={register("email")} error={formState.errors.email?.message} placeholder="name@example.com" />
        <button disabled={fetcher.state !== "idle"} type="submit" tabIndex={0} className="btn btn-primary mt-2">
          Continue
        </button>
      </fetcher.Form>
      <div className="mt-2 text-center">
        Don't have an account yet?
        <br />
        Please{" "}
        <span>
          <Link className="link-info" to="/register">
            register here
          </Link>
        </span>
      </div>
    </div>
  );
}
