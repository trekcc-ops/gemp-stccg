import globals from "globals";
import pluginJs from "@eslint/js";
import jquery from "eslint-plugin-jquery";


export default [
  {
    languageOptions: { 
      globals: {
        ...globals.browser,
        ...globals.jquery
      }
    },
    ignores: ["*/jquery/*"]
  },
  pluginJs.configs.recommended,
  {
    plugins: {
      jquery
    },
    rules: jquery.configs.deprecated.rules
  }
];