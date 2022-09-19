<!--
  - Copyright 2022, TeamDev. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Redistribution and use in source and/or binary forms, with or without
  - modification, must retain the above copyright notice and the following
  - disclaimer.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  - LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  - A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  - OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  - SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  - LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  - DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  - THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  - (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  - OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  -->

<template>
  <div id="header">
    <div class="logo">
      <a href="/">
        <img id="logo" src="../assets/logo.svg" alt="Spine Event Engine" />
      </a>
    </div>
  </div>
</template>

<script lang="ts">
/**
 * Displays the website header with the Spine logo.
 */
export default {
  name: "TheHeader",
};
</script>

<style scoped>
#logo {
  height: 80px;
  padding-left: 20px;
}

#header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  height: 100px;
  background-color: #1378da;
  margin: 0 auto 30px;
}
</style>
