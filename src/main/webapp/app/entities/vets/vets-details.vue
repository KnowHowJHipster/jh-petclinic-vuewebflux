<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="vets">
        <h2 class="jh-entity-heading" data-cy="vetsDetailsHeading">
          <span v-text="$t('petclinicApp.vets.detail.title')">Vets</span> {{ vets.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="$t('petclinicApp.vets.firstname')">Firstname</span>
          </dt>
          <dd>
            <span>{{ vets.firstname }}</span>
          </dd>
          <dt>
            <span v-text="$t('petclinicApp.vets.lastname')">Lastname</span>
          </dt>
          <dd>
            <span>{{ vets.lastname }}</span>
          </dd>
        </dl>
        <button type="submit" v-on:click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
        </button>
        <router-link v-if="vets.id" :to="{ name: 'VetsEdit', params: { vetsId: vets.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./vets-details.component.ts"></script>
