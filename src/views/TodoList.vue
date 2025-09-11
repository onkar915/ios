<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Widget Demo App</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content class="ion-padding">
      <div class="container">
        <h2>iOS Widget Demo</h2>
        <p>This app includes a native iOS widget that displays a greeting message.</p>
        
        <ion-card>
          <ion-card-header>
            <ion-card-title>Widget Preview</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="widget-preview">
              <div class="widget-small">
                <h3>Hello, User!</h3>
                <p>From Ionic App</p>
              </div>
            </div>
            <p class="ion-margin-top">
              <ion-text color="medium">
                This is a hardcoded widget that doesn't require Xcode for setup.
              </ion-text>
            </p>
          </ion-card-content>
        </ion-card>
        
        <ion-card>
          <ion-card-header>
            <ion-card-title>Build Instructions</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ol>
              <li>Run <code>ionic build</code></li>
              <li>Run <code>npx cap sync</code></li>
              <li>Use AppFlow to build your iOS app</li>
              <li>The widget will be included automatically</li>
            </ol>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonText
} from '@ionic/vue';
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: 0 auto;
}

.widget-preview {
  display: flex;
  justify-content: center;
}

.widget-small {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #3880ff 0%, #3dc2ff 100%);
  border-radius: 20px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.widget-small h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  text-align: center;
  font-weight: 600;
}

.widget-small p {
  margin: 0;
  font-size: 12px;
  opacity: 0.9;
}
</style>