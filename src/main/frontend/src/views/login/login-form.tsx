import { useFetcher } from "react-router-dom";
import { z } from "zod";
import { useForm } from "react-hook-form";
import TextInput from "../../components/form/text-input.tsx";
import { FaLock } from "react-icons/fa";
import { zodResolver } from "@hookform/resolvers/zod";

const schema = z.object({
  email: z.string().email(),
});

type Schema = z.infer<typeof schema>;

export default function LoginForm() {
  const fetcher = useFetcher();
  const { register, handleSubmit, formState } = useForm<Schema>({ resolver: zodResolver(schema) });

  // TODO add toast error handler

  function onSubmit(data: Schema) {
    fetcher.submit(data, { method: "post", action: "/login", encType: "application/json" });
  }

  return (
    <div className="flex h-full flex-col items-center justify-center gap-3">
      <FaLock size={55} className="text-secondary" />
      <h1 className="text-lg uppercase">Login</h1>
      <fetcher.Form onSubmit={handleSubmit(onSubmit)} className="flex w-full max-w-md flex-col gap-2">
        <TextInput register={register("email")} error={formState.errors.email?.message} placeholder="name@example.com" />
        <button disabled={fetcher.state !== "idle"} type="submit" tabIndex={0} className="btn btn-primary mt-2">
          Submit
        </button>
      </fetcher.Form>
    </div>
  );
}
